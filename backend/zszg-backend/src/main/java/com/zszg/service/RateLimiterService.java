package com.zszg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API限流服务
 * 
 * 功能:
 * 1. 基于用户的限流 (防止单个用户过度调用)
 * 2. 基于IP的限流 (防止恶意攻击)
 * 3. 基于API的限流 (保护重要接口)
 * 4. 滑动窗口算法实现
 */
@Slf4j
@Service
public class RateLimiterService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 限流配置
    private static final int AI_ANALYSIS_LIMIT_PER_MINUTE = 10;    // AI分析：10次/分钟
    private static final int PHOTO_SEARCH_LIMIT_PER_MINUTE = 5;    // 拍照搜题：5次/分钟
    private static final int GENERAL_API_LIMIT_PER_MINUTE = 60;    // 一般API：60次/分钟
    
    private final CacheService cacheService;
    
    public RateLimiterService(RedisTemplate<String, Object> redisTemplate, CacheService cacheService) {
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
    }
    
    /**
     * 检查是否允许请求（AI分析接口）
     */
    public boolean allowAIAnalysis(Long userId) {
        return checkRateLimit("ai:analysis:" + userId, AI_ANALYSIS_LIMIT_PER_MINUTE, 60);
    }
    
    /**
     * 检查是否允许请求（拍照搜题接口）
     */
    public boolean allowPhotoSearch(Long userId) {
        return checkRateLimit("photo:search:" + userId, PHOTO_SEARCH_LIMIT_PER_MINUTE, 60);
    }
    
    /**
     * 检查是否允许请求（一般API接口）
     */
    public boolean allowGeneralAPI(Long userId) {
        return checkRateLimit("api:general:" + userId, GENERAL_API_LIMIT_PER_MINUTE, 60);
    }
    
    /**
     * 检查是否允许请求（基于IP）
     */
    public boolean allowByIP(String ip, int maxRequests, int windowSeconds) {
        return checkRateLimit("ip:" + ip, maxRequests, windowSeconds);
    }
    
    /**
     * 核心限流检查逻辑（滑动窗口算法）
     * 
     * @param key Redis key
     * @param maxRequests 最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许请求
     */
    private boolean checkRateLimit(String key, int maxRequests, int windowSeconds) {
        // 如果Redis不可用，直接允许（降级策略）
        if (!cacheService.isRedisAvailable()) {
            log.warn("⚠️ Redis不可用，限流功能降级");
            return true;
        }
        
        try {
            String rateLimitKey = "ratelimit:" + key;
            long now = System.currentTimeMillis();
            long windowStart = now - windowSeconds * 1000L;
            
            // 移除过期的记录
            redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long currentCount = redisTemplate.opsForZSet().zCard(rateLimitKey);
            
            if (currentCount != null && currentCount >= maxRequests) {
                log.warn("⚠️ 触发限流 - key: {}, 当前请求数: {}, 限制: {}/{秒}", 
                        key, currentCount, maxRequests, windowSeconds);
                return false;
            }
            
            // 添加当前请求
            redisTemplate.opsForZSet().add(rateLimitKey, String.valueOf(now), now);
            
            // 设置key过期时间
            redisTemplate.expire(rateLimitKey, windowSeconds, TimeUnit.SECONDS);
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ 限流检查失败", e);
            // 失败时允许请求（降级策略）
            return true;
        }
    }
    
    /**
     * 获取剩余请求次数
     */
    public int getRemainingRequests(String key, int maxRequests, int windowSeconds) {
        if (!cacheService.isRedisAvailable()) {
            return maxRequests;
        }
        
        try {
            String rateLimitKey = "ratelimit:" + key;
            long now = System.currentTimeMillis();
            long windowStart = now - windowSeconds * 1000L;
            
            // 移除过期的记录
            redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long currentCount = redisTemplate.opsForZSet().zCard(rateLimitKey);
            
            return maxRequests - (currentCount != null ? currentCount.intValue() : 0);
            
        } catch (Exception e) {
            log.error("❌ 获取剩余请求数失败", e);
            return maxRequests;
        }
    }
    
    /**
     * 重置限流计数（管理员功能）
     */
    public void resetRateLimit(String key) {
        if (!cacheService.isRedisAvailable()) {
            return;
        }
        
        try {
            String rateLimitKey = "ratelimit:" + key;
            redisTemplate.delete(rateLimitKey);
            log.info("✅ 重置限流计数 - key: {}", key);
        } catch (Exception e) {
            log.error("❌ 重置限流计数失败", e);
        }
    }
}























