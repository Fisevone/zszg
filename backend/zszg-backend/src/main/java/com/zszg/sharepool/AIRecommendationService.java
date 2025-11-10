package com.zszg.sharepool;

import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI智能推荐服务
 * 基于用户的错题数据，智能推荐共享池中的优质内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {
    private final ErrorBookRepository errorBookRepository;
    private final SharePoolRepository sharePoolRepository;
    private final ShareInteractionRepository shareInteractionRepository;

    /**
     * 为用户推荐共享池内容
     * 推荐逻辑：
     * 1. 分析用户的错题分布（学科、难度、知识点）
     * 2. 推荐相同学科、相似难度的高质量错题
     * 3. 优先推荐点赞数多、浏览数高的题目
     * 4. 排除用户已收藏的题目
     */
    public List<SharePool> recommendForUser(User user) {
        log.info("🤖 AI开始为用户 {} 推荐内容...", user.getUsername());
        
        // 1. 获取用户的错题数据
        List<ErrorBook> userErrors = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        // 2. 分析用户的学科偏好
        Map<String, Long> subjectCount = userErrors.stream()
                .filter(e -> e.getQuestion() != null && e.getQuestion().getSubject() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getQuestion().getSubject(),
                        Collectors.counting()
                ));
        
        // 3. 分析用户的难度偏好
        Map<String, Long> difficultyCount = userErrors.stream()
                .filter(e -> e.getQuestion() != null && e.getQuestion().getDifficulty() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getQuestion().getDifficulty(),
                        Collectors.counting()
                ));
        
        log.info("📊 用户学科分布: {}", subjectCount);
        log.info("📊 用户难度分布: {}", difficultyCount);
        
        // 4. 获取所有已审核的共享内容
        List<SharePool> allShares = sharePoolRepository.findByApprovedTrueOrderByCreatedAtDesc();
        
        // 5. 获取用户已收藏的内容
        Set<Long> favoritedIds = shareInteractionRepository.findUserFavorites(user)
                .stream()
                .map(SharePool::getId)
                .collect(Collectors.toSet());
        
        // 6. 计算推荐分数并排序
        List<SharePool> recommendations = allShares.stream()
                .filter(share -> !favoritedIds.contains(share.getId())) // 排除已收藏
                .filter(share -> share.getErrorBook() != null && share.getErrorBook().getQuestion() != null)
                .map(share -> {
                    double score = calculateRecommendationScore(share, subjectCount, difficultyCount);
                    return new ScoredShare(share, score);
                })
                .sorted(Comparator.comparingDouble(ScoredShare::getScore).reversed())
                .limit(20) // 限制推荐数量
                .map(ScoredShare::getShare)
                .collect(Collectors.toList());
        
        log.info("✅ AI推荐完成，共推荐 {} 条内容", recommendations.size());
        return recommendations;
    }
    
    /**
     * 计算推荐分数
     */
    private double calculateRecommendationScore(SharePool share, 
                                                Map<String, Long> subjectCount, 
                                                Map<String, Long> difficultyCount) {
        double score = 0.0;
        
        String subject = share.getErrorBook().getQuestion().getSubject();
        String difficulty = share.getErrorBook().getQuestion().getDifficulty();
        
        // 学科匹配加分（权重：40%）
        if (subject != null && subjectCount.containsKey(subject)) {
            long count = subjectCount.get(subject);
            score += count * 0.4;
        }
        
        // 难度匹配加分（权重：20%）
        if (difficulty != null && difficultyCount.containsKey(difficulty)) {
            long count = difficultyCount.get(difficulty);
            score += count * 0.2;
        }
        
        // 点赞数加分（权重：20%）
        score += share.getLikes() * 0.2;
        
        // 收藏数加分（权重：15%）
        score += share.getFavorites() * 0.15;
        
        // 浏览数加分（权重：5%）
        score += share.getViews() * 0.05;
        
        return score;
    }
    
    /**
     * 内部类：带分数的共享内容
     */
    private static class ScoredShare {
        private final SharePool share;
        private final double score;
        
        public ScoredShare(SharePool share, double score) {
            this.share = share;
            this.score = score;
        }
        
        public SharePool getShare() {
            return share;
        }
        
        public double getScore() {
            return score;
        }
    }
}






