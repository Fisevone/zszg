package com.zszg.sharepool;

import com.zszg.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareInteractionRepository extends JpaRepository<ShareInteraction, Long> {
    
    /**
     * 查找用户对某个分享的特定交互
     */
    Optional<ShareInteraction> findByUserAndSharePoolAndInteractionType(
            User user, SharePool sharePool, String interactionType);
    
    /**
     * 检查用户是否已点赞
     */
    @Query("SELECT CASE WHEN COUNT(si) > 0 THEN true ELSE false END FROM ShareInteraction si " +
           "WHERE si.user = :user AND si.sharePool.id = :sharePoolId AND si.interactionType = 'LIKE'")
    boolean hasUserLiked(@Param("user") User user, @Param("sharePoolId") Long sharePoolId);
    
    /**
     * 检查用户是否已收藏
     */
    @Query("SELECT CASE WHEN COUNT(si) > 0 THEN true ELSE false END FROM ShareInteraction si " +
           "WHERE si.user = :user AND si.sharePool.id = :sharePoolId AND si.interactionType = 'FAVORITE'")
    boolean hasUserFavorited(@Param("user") User user, @Param("sharePoolId") Long sharePoolId);
    
    /**
     * 获取用户的所有收藏
     */
    @Query("SELECT si.sharePool FROM ShareInteraction si " +
           "WHERE si.user = :user AND si.interactionType = 'FAVORITE' " +
           "ORDER BY si.createdAt DESC")
    List<SharePool> findUserFavorites(@Param("user") User user);
    
    /**
     * 获取用户的所有点赞
     */
    @Query("SELECT si.sharePool FROM ShareInteraction si " +
           "WHERE si.user = :user AND si.interactionType = 'LIKE' " +
           "ORDER BY si.createdAt DESC")
    List<SharePool> findUserLikes(@Param("user") User user);
    
    /**
     * 删除用户的交互记录
     */
    void deleteByUserAndSharePoolAndInteractionType(User user, SharePool sharePool, String interactionType);
}






