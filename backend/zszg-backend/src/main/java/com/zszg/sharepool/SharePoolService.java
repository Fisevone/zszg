package com.zszg.sharepool;

import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharePoolService {
    private final SharePoolRepository sharePoolRepository;
    private final ErrorBookRepository errorBookRepository;
    private final ShareInteractionRepository shareInteractionRepository;

    /**
     * 获取已审核的共享错题
     */
    public List<SharePool> getApprovedShares() {
        return sharePoolRepository.findByApprovedTrueOrderByCreatedAtDesc();
    }

    /**
     * 获取待审核的共享错题
     */
    public List<SharePool> getPendingShares() {
        return sharePoolRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    /**
     * 按范围筛选
     */
    public List<SharePool> getSharesByScope(String scope) {
        return sharePoolRepository.findByScope(scope);
    }

    /**
     * 按学科筛选
     */
    public List<SharePool> getSharesBySubject(String subject) {
        return sharePoolRepository.findBySubject(subject);
    }

    /**
     * 分享错题到共享池
     */
    @Transactional
    public SharePool shareErrorBook(Long errorBookId, String scope, String tags, User user) {
        ErrorBook errorBook = errorBookRepository.findById(errorBookId)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        // 检查用户权限：只能分享自己的错题
        if (!errorBook.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("只能分享自己的错题");
        }
        
        // 检查是否已经分享过（防止重复分享）
        List<SharePool> existingShares = sharePoolRepository.findByErrorBookId(errorBookId);
        if (!existingShares.isEmpty()) {
            throw new RuntimeException("该错题已经分享过了");
        }
        
        // 检查错题状态，如果已经是SHARED状态，说明已经分享过
        if ("SHARED".equals(errorBook.getStatus())) {
            throw new RuntimeException("该错题已经分享过了");
        }
        
        // 更新错题状态为已共享
        errorBook.setStatus("SHARED");
        errorBookRepository.save(errorBook);
        
        SharePool sharePool = SharePool.builder()
                .errorBook(errorBook)
                .scope(scope)
                .tags(tags)
                .approved(false)
                .build();
        
        SharePool savedSharePool = sharePoolRepository.save(sharePool);
        
        log.info("✅ 用户 {} 分享错题 {} 到共享池，范围: {}, 标签: {}", 
                 user.getUsername(), errorBookId, scope, tags);
        
        return savedSharePool;
    }

    /**
     * 审核共享（通过/拒绝）
     */
    @Transactional
    public SharePool reviewShare(Long shareId, User reviewer, boolean approve) {
        SharePool sharePool = sharePoolRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("共享记录不存在"));
        
        sharePool.setApproved(approve);
        sharePool.setApprovedBy(reviewer);
        sharePool.setApprovedAt(Instant.now());
        
        return sharePoolRepository.save(sharePool);
    }

    /**
     * 点赞（改进版：防止重复点赞）
     */
    @Transactional
    public SharePool likeShare(Long shareId, User user) {
        SharePool sharePool = sharePoolRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("共享记录不存在"));
        
        // 检查是否已点赞
        boolean hasLiked = shareInteractionRepository.hasUserLiked(user, shareId);
        if (hasLiked) {
            log.warn("用户 {} 重复点赞 SharePool {}", user.getUsername(), shareId);
            throw new RuntimeException("你已经点赞过了");
        }
        
        // 创建点赞记录
        ShareInteraction interaction = ShareInteraction.builder()
                .user(user)
                .sharePool(sharePool)
                .interactionType("LIKE")
                .build();
        shareInteractionRepository.save(interaction);
        
        // 增加点赞数
        sharePool.setLikes(sharePool.getLikes() + 1);
        sharePoolRepository.save(sharePool);
        
        log.info("✅ 用户 {} 点赞 SharePool {}, 当前点赞数: {}", 
                 user.getUsername(), shareId, sharePool.getLikes());
        return sharePool;
    }

    /**
     * 收藏/取消收藏（改进版：支持取消）
     */
    @Transactional
    public SharePool favoriteShare(Long shareId, User user) {
        SharePool sharePool = sharePoolRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("共享记录不存在"));
        
        // 检查是否已收藏
        boolean hasFavorited = shareInteractionRepository.hasUserFavorited(user, shareId);
        
        if (hasFavorited) {
            // 已收藏，执行取消收藏
            shareInteractionRepository.deleteByUserAndSharePoolAndInteractionType(user, sharePool, "FAVORITE");
            
            // 减少收藏数
            if (sharePool.getFavorites() > 0) {
                sharePool.setFavorites(sharePool.getFavorites() - 1);
            }
            sharePoolRepository.save(sharePool);
            
            log.info("❌ 用户 {} 取消收藏 SharePool {}, 当前收藏数: {}", 
                     user.getUsername(), shareId, sharePool.getFavorites());
        } else {
            // 未收藏，执行收藏
            ShareInteraction interaction = ShareInteraction.builder()
                    .user(user)
                    .sharePool(sharePool)
                    .interactionType("FAVORITE")
                    .build();
            shareInteractionRepository.save(interaction);
            
            // 增加收藏数
            sharePool.setFavorites(sharePool.getFavorites() + 1);
            sharePoolRepository.save(sharePool);
            
            log.info("✅ 用户 {} 收藏 SharePool {}, 当前收藏数: {}", 
                     user.getUsername(), shareId, sharePool.getFavorites());
        }
        
        return sharePool;
    }
    
    /**
     * 增加浏览次数
     */
    @Transactional
    public SharePool incrementViews(Long shareId) {
        SharePool sharePool = sharePoolRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("共享记录不存在"));
        
        sharePool.setViews(sharePool.getViews() + 1);
        return sharePoolRepository.save(sharePool);
    }
    
    /**
     * 获取用户的收藏列表
     */
    public List<SharePool> getUserFavorites(User user) {
        return shareInteractionRepository.findUserFavorites(user);
    }
    
    /**
     * 检查用户是否已点赞
     */
    public boolean hasUserLiked(User user, Long shareId) {
        return shareInteractionRepository.hasUserLiked(user, shareId);
    }
    
    /**
     * 检查用户是否已收藏
     */
    public boolean hasUserFavorited(User user, Long shareId) {
        return shareInteractionRepository.hasUserFavorited(user, shareId);
    }

    /**
     * 删除共享
     */
    @Transactional
    public void deleteShare(Long shareId) {
        sharePoolRepository.deleteById(shareId);
    }
}


