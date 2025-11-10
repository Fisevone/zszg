package com.zszg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 * 
 * 功能:
 * 1. AI分析结果缓存
 * 2. 知识点提取缓存
 * 3. 推荐题目缓存
 * 4. 自动过期管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存key前缀
    private static final String ANALYSIS_PREFIX = "ai:analysis:";
    private static final String KNOWLEDGE_PREFIX = "ai:knowledge:";
    private static final String RECOMMEND_PREFIX = "ai:recommend:";
    private static final String MINDMAP_PREFIX = "ai:mindmap:";
    private static final String PREDICTION_PREFIX = "ai:prediction:";
    
    // 默认过期时间（天）
    private static final int ANALYSIS_EXPIRE_DAYS = 7;      // AI分析缓存7天
    private static final int KNOWLEDGE_EXPIRE_DAYS = 30;    // 知识点缓存30天
    private static final int RECOMMEND_EXPIRE_HOURS = 1;    // 推荐缓存1小时
    private static final int MINDMAP_EXPIRE_DAYS = 7;       // 思维导图缓存7天
    private static final int PREDICTION_EXPIRE_HOURS = 12;  // 预测缓存12小时
    
    /**
     * 检查Redis是否可用
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", 10, TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get("health:check");
            return "ok".equals(value);
        } catch (Exception e) {
            log.warn("⚠️ Redis不可用: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取或生成AI分析结果（带缓存）
     */
    public String getOrGenerateAnalysis(String questionContent, String correctAnswer, 
                                       String userAnswer, String difficulty,
                                       AnalysisGenerator generator) {
        if (!isRedisAvailable()) {
            log.warn("⚠️ Redis不可用，直接生成分析");
            return generator.generate();
        }
        
        // 生成缓存key（基于题目内容+答案的MD5）
        String cacheKey = ANALYSIS_PREFIX + generateMD5(questionContent + correctAnswer + userAnswer);
        
        // 先查缓存
        String cachedAnalysis = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAnalysis != null) {
            log.info("✅ 命中分析缓存: {}", cacheKey);
            return cachedAnalysis;
        }
        
        // 缓存未命中，生成分析
        log.info("⚠️ 缓存未命中，生成新分析: {}", cacheKey);
        String analysis = generator.generate();
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, analysis, ANALYSIS_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("✅ 分析结果已缓存，过期时间: {}天", ANALYSIS_EXPIRE_DAYS);
        
        return analysis;
    }
    
    /**
     * 获取或提取知识点（带缓存）
     */
    public Object getOrExtractKnowledge(String subject, String questionContent, 
                                       KnowledgeExtractor extractor) {
        if (!isRedisAvailable()) {
            return extractor.extract();
        }
        
        String cacheKey = KNOWLEDGE_PREFIX + generateMD5(subject + questionContent);
        
        // 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("✅ 命中知识点缓存: {}", cacheKey);
            return cached;
        }
        
        // 缓存未命中，提取知识点
        log.info("⚠️ 缓存未命中，提取新知识点: {}", cacheKey);
        Object knowledge = extractor.extract();
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, knowledge, KNOWLEDGE_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("✅ 知识点已缓存，过期时间: {}天", KNOWLEDGE_EXPIRE_DAYS);
        
        return knowledge;
    }
    
    /**
     * 获取或生成推荐（带缓存）
     */
    public Object getOrGenerateRecommendation(Long userId, String subject, 
                                             RecommendationGenerator generator) {
        if (!isRedisAvailable()) {
            return generator.generate();
        }
        
        String cacheKey = RECOMMEND_PREFIX + userId + ":" + subject;
        
        // 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("✅ 命中推荐缓存: {}", cacheKey);
            return cached;
        }
        
        // 缓存未命中，生成推荐
        log.info("⚠️ 缓存未命中，生成新推荐: {}", cacheKey);
        Object recommendation = generator.generate();
        
        // 存入缓存（较短过期时间，因为推荐会随着学习变化）
        redisTemplate.opsForValue().set(cacheKey, recommendation, 
                                       RECOMMEND_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("✅ 推荐已缓存，过期时间: {}小时", RECOMMEND_EXPIRE_HOURS);
        
        return recommendation;
    }
    
    /**
     * 获取或生成思维导图（带缓存）
     */
    public Object getOrGenerateMindMap(String content, String subject, 
                                      MindMapGenerator generator) {
        if (!isRedisAvailable()) {
            return generator.generate();
        }
        
        String cacheKey = MINDMAP_PREFIX + generateMD5(subject + content);
        
        // 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("✅ 命中思维导图缓存: {}", cacheKey);
            return cached;
        }
        
        // 缓存未命中，生成思维导图
        log.info("⚠️ 缓存未命中，生成新思维导图: {}", cacheKey);
        Object mindMap = generator.generate();
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, mindMap, MINDMAP_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("✅ 思维导图已缓存，过期时间: {}天", MINDMAP_EXPIRE_DAYS);
        
        return mindMap;
    }
    
    /**
     * 获取或生成预测报告（带缓存）
     */
    public Object getOrGeneratePrediction(Long userId, String subject, 
                                         PredictionGenerator generator) {
        if (!isRedisAvailable()) {
            return generator.generate();
        }
        
        String cacheKey = PREDICTION_PREFIX + userId + ":" + subject;
        
        // 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("✅ 命中预测缓存: {}", cacheKey);
            return cached;
        }
        
        // 缓存未命中，生成预测
        log.info("⚠️ 缓存未命中，生成新预测: {}", cacheKey);
        Object prediction = generator.generate();
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, prediction, 
                                       PREDICTION_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("✅ 预测已缓存，过期时间: {}小时", PREDICTION_EXPIRE_HOURS);
        
        return prediction;
    }
    
    /**
     * 清除用户相关的所有缓存（当用户有新错题时）
     */
    public void clearUserCache(Long userId) {
        if (!isRedisAvailable()) {
            return;
        }
        
        try {
            // 清除推荐缓存
            redisTemplate.delete(RECOMMEND_PREFIX + userId + ":*");
            // 清除预测缓存
            redisTemplate.delete(PREDICTION_PREFIX + userId + ":*");
            log.info("✅ 已清除用户{}的缓存", userId);
        } catch (Exception e) {
            log.error("清除缓存失败", e);
        }
    }
    
    /**
     * 生成MD5哈希（用于缓存key）
     */
    private String generateMD5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        if (!isRedisAvailable()) {
            stats.setAvailable(false);
            return stats;
        }
        
        try {
            stats.setAvailable(true);
            
            // 统计各类缓存数量
            stats.setAnalysisCount(countKeys(ANALYSIS_PREFIX));
            stats.setKnowledgeCount(countKeys(KNOWLEDGE_PREFIX));
            stats.setRecommendCount(countKeys(RECOMMEND_PREFIX));
            stats.setMindMapCount(countKeys(MINDMAP_PREFIX));
            stats.setPredictionCount(countKeys(PREDICTION_PREFIX));
            
            stats.setTotalCount(stats.getAnalysisCount() + stats.getKnowledgeCount() + 
                               stats.getRecommendCount() + stats.getMindMapCount() + 
                               stats.getPredictionCount());
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            stats.setAvailable(false);
        }
        
        return stats;
    }
    
    /**
     * 统计指定前缀的key数量
     */
    private long countKeys(String prefix) {
        try {
            var keys = redisTemplate.keys(prefix + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // 函数式接口，用于延迟执行
    @FunctionalInterface
    public interface AnalysisGenerator {
        String generate();
    }
    
    @FunctionalInterface
    public interface KnowledgeExtractor {
        Object extract();
    }
    
    @FunctionalInterface
    public interface RecommendationGenerator {
        Object generate();
    }
    
    @FunctionalInterface
    public interface MindMapGenerator {
        Object generate();
    }
    
    @FunctionalInterface
    public interface PredictionGenerator {
        Object generate();
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private boolean available;
        private long analysisCount;
        private long knowledgeCount;
        private long recommendCount;
        private long mindMapCount;
        private long predictionCount;
        private long totalCount;
        
        // Getters and Setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public long getAnalysisCount() { return analysisCount; }
        public void setAnalysisCount(long analysisCount) { this.analysisCount = analysisCount; }
        
        public long getKnowledgeCount() { return knowledgeCount; }
        public void setKnowledgeCount(long knowledgeCount) { this.knowledgeCount = knowledgeCount; }
        
        public long getRecommendCount() { return recommendCount; }
        public void setRecommendCount(long recommendCount) { this.recommendCount = recommendCount; }
        
        public long getMindMapCount() { return mindMapCount; }
        public void setMindMapCount(long mindMapCount) { this.mindMapCount = mindMapCount; }
        
        public long getPredictionCount() { return predictionCount; }
        public void setPredictionCount(long predictionCount) { this.predictionCount = predictionCount; }
        
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }
}























