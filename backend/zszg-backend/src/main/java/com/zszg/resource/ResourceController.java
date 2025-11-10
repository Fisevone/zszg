package com.zszg.resource;

import com.zszg.common.ApiResponse;
import com.zszg.service.FileStorageService;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    /**
     * 获取已审核的资源
     */
    @GetMapping
    public ApiResponse<List<ResourceItem>> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type) {
        
        if (subject != null && !subject.isEmpty()) {
            return ApiResponse.ok(resourceService.getResourcesBySubject(subject));
        }
        if (type != null && !type.isEmpty()) {
            return ApiResponse.ok(resourceService.getResourcesByType(type));
        }
        return ApiResponse.ok(resourceService.getApprovedResources());
    }

    /**
     * 获取待审核的资源（管理员）
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ResourceItem>> pending() {
        return ApiResponse.ok(resourceService.getPendingResources());
    }

    /**
     * 搜索资源
     */
    @GetMapping("/search")
    public ApiResponse<List<ResourceItem>> search(@RequestParam String keyword) {
        return ApiResponse.ok(resourceService.searchResources(keyword));
    }

    /**
     * 上传资源（文件上传 + 创建资源记录）
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<ResourceItem> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam("subject") String subject,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            // 1. 上传文件
            String fileUrl = fileStorageService.storeFile(file, "resources");
            
            // 2. 创建资源记录
            User uploader = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            ResourceItem resource = resourceService.uploadResource(
                    uploader, title, type, subject, fileUrl
            );
            
            // 教师端上传的资源自动审核通过
            if (uploader.getRole().equals("ROLE_TEACHER") || uploader.getRole().equals("ROLE_ADMIN")) {
                resource = resourceService.reviewResource(resource.getId(), uploader, true);
            }
            
            return ApiResponse.ok(resource);
        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 创建资源
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<ResourceItem> create(
            @Valid @RequestBody CreateResourceDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        User uploader = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        ResourceItem resource = resourceService.uploadResource(
                uploader, dto.getTitle(), dto.getType(), dto.getSubject(), dto.getFileUrl()
        );
        return ApiResponse.ok(resource);
    }

    /**
     * 审核资源（管理员）
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ResourceItem> review(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @AuthenticationPrincipal UserDetails principal) {
        User reviewer = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        ResourceItem resource = resourceService.reviewResource(id, reviewer, approve);
        return ApiResponse.ok(resource);
    }

    /**
     * 更新资源
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<ResourceItem> update(
            @PathVariable Long id,
            @RequestBody UpdateResourceDto dto) {
        ResourceItem resource = resourceService.updateResource(id, dto.getTitle(), dto.getSubject());
        return ApiResponse.ok(resource);
    }

    /**
     * 删除资源（教师和管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateResourceDto {
        @NotBlank
        private String title;
        @NotBlank
        private String type; // PAPER / VIDEO / NOTES
        private String subject;
        @NotBlank
        private String fileUrl;
    }

    @Data
    public static class UpdateResourceDto {
        private String title;
        private String subject;
    }
}
