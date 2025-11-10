package com.zszg.sharepool;

import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/share-pool")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SharePoolController {
    private final SharePoolService sharePoolService;
    private final UserRepository userRepository;

    /**
     * 获取已审核的共享错题列表
     */
    @GetMapping
    public ApiResponse<List<SharePool>> getApprovedShares(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String subject
    ) {
        if (scope != null) {
            return ApiResponse.ok(sharePoolService.getSharesByScope(scope));
        }
        if (subject != null) {
            return ApiResponse.ok(sharePoolService.getSharesBySubject(subject));
        }
        return ApiResponse.ok(sharePoolService.getApprovedShares());
    }

    /**
     * 获取待审核的共享错题（管理员/教师）
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<SharePool>> getPendingShares() {
        return ApiResponse.ok(sharePoolService.getPendingShares());
    }

    /**
     * 分享错题到共享池
     */
    @PostMapping
    public ApiResponse<SharePool> shareErrorBook(
            @RequestBody ShareDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            User user = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            SharePool sharePool = sharePoolService.shareErrorBook(
                    dto.getErrorBookId(),
                    dto.getScope(),
                    dto.getTags(),
                    user
            );
            return ApiResponse.ok(sharePool);
        } catch (RuntimeException e) {
            log.error("分享错题失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 审核共享（管理员/教师）
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<SharePool> reviewShare(
            @PathVariable Long id,
            @RequestBody ReviewDto dto,
            @AuthenticationPrincipal User user
    ) {
        SharePool sharePool = sharePoolService.reviewShare(id, user, dto.isApprove());
        return ApiResponse.ok(sharePool);
    }

    /**
     * 点赞（改进版：带用户信息，防止重复）
     */
    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> likeShare(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            SharePool sharePool = sharePoolService.likeShare(id, user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sharePool", sharePool);
            result.put("likes", sharePool.getLikes());
            result.put("message", "点赞成功");
            
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 收藏/取消收藏（改进版：带用户信息，支持取消）
     */
    @PostMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> favoriteShare(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            boolean wasFavorited = sharePoolService.hasUserFavorited(user, id);
            
            SharePool sharePool = sharePoolService.favoriteShare(id, user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sharePool", sharePool);
            result.put("favorites", sharePool.getFavorites());
            result.put("favorited", !wasFavorited); // 当前状态
            result.put("message", wasFavorited ? "取消收藏成功" : "收藏成功");
            
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 增加浏览次数
     */
    @PostMapping("/{id}/view")
    public ApiResponse<SharePool> incrementViews(@PathVariable Long id) {
        try {
            SharePool sharePool = sharePoolService.incrementViews(id);
            return ApiResponse.ok(sharePool);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的收藏列表
     */
    @GetMapping("/my-favorites")
    public ApiResponse<List<SharePool>> getMyFavorites(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        List<SharePool> favorites = sharePoolService.getUserFavorites(user);
        return ApiResponse.ok(favorites);
    }
    
    /**
     * 检查用户的交互状态（是否已点赞、已收藏）
     */
    @GetMapping("/{id}/interaction-status")
    public ApiResponse<Map<String, Boolean>> getInteractionStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        
        Map<String, Boolean> status = new HashMap<>();
        status.put("liked", sharePoolService.hasUserLiked(user, id));
        status.put("favorited", sharePoolService.hasUserFavorited(user, id));
        
        return ApiResponse.ok(status);
    }

    /**
     * 删除共享（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteShare(@PathVariable Long id) {
        sharePoolService.deleteShare(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class ShareDto {
        private Long errorBookId;
        private String scope;
        private String tags;
    }

    @Data
    public static class ReviewDto {
        private boolean approve;
    }
}
