package com.zszg.knowledge;

import com.zszg.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    /**
     * 获取所有知识点
     */
    @GetMapping
    public ApiResponse<List<Knowledge>> list(@RequestParam(required = false) String subject) {
        if (subject != null && !subject.isEmpty()) {
            return ApiResponse.ok(knowledgeService.getKnowledgeBySubject(subject));
        }
        return ApiResponse.ok(knowledgeService.getAllKnowledge());
    }

    /**
     * 获取根知识点
     */
    @GetMapping("/root")
    public ApiResponse<List<Knowledge>> getRoots(@RequestParam String subject) {
        return ApiResponse.ok(knowledgeService.getRootKnowledge(subject));
    }

    /**
     * 获取子知识点
     */
    @GetMapping("/{id}/children")
    public ApiResponse<List<Knowledge>> getChildren(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeService.getChildrenKnowledge(id));
    }

    /**
     * 搜索知识点
     */
    @GetMapping("/search")
    public ApiResponse<List<Knowledge>> search(@RequestParam String keyword) {
        return ApiResponse.ok(knowledgeService.searchKnowledge(keyword));
    }

    /**
     * 创建知识点（管理员/教师）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Knowledge> create(@Valid @RequestBody CreateKnowledgeDto dto) {
        Knowledge knowledge = knowledgeService.createKnowledge(
                dto.getSubject(), dto.getTitle(), dto.getCode(), dto.getParentId(),
                dto.getTextbookRef(), dto.getTeacherNotesUrl(), dto.getVideoUrl()
        );
        return ApiResponse.ok(knowledge);
    }

    /**
     * 更新知识点（管理员/教师）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Knowledge> update(
            @PathVariable Long id,
            @RequestBody UpdateKnowledgeDto dto) {
        Knowledge knowledge = knowledgeService.updateKnowledge(
                id, dto.getTitle(), dto.getTextbookRef(),
                dto.getTeacherNotesUrl(), dto.getVideoUrl()
        );
        return ApiResponse.ok(knowledge);
    }

    /**
     * 删除知识点（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateKnowledgeDto {
        @NotBlank
        private String subject;
        @NotBlank
        private String title;
        private String code;
        private Long parentId;
        private String textbookRef;
        private String teacherNotesUrl;
        private String videoUrl;
    }

    @Data
    public static class UpdateKnowledgeDto {
        private String title;
        private String textbookRef;
        private String teacherNotesUrl;
        private String videoUrl;
    }
}
