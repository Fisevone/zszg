package com.zszg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 重试服务
 * 
 * 功能:
 * 1. API调用失败自动重试
 * 2. 指数退避策略
 * 3. 最大重试次数限制
 * 4. 异常分类处理
 */
@Slf4j
@Service
public class RetryService {
    
    // 默认配置
    private static final int DEFAULT_MAX_RETRIES = 3;           // 最大重试次数
    private static final long DEFAULT_INITIAL_DELAY = 1000;     // 初始延迟（毫秒）
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0; // 退避倍数
    
    /**
     * 执行带重试的操作（默认配置）
     */
    public <T> T executeWithRetry(Supplier<T> operation) {
        return executeWithRetry(operation, DEFAULT_MAX_RETRIES, 
                               DEFAULT_INITIAL_DELAY, DEFAULT_BACKOFF_MULTIPLIER);
    }
    
    /**
     * 执行带重试的操作（自定义配置）
     * 
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟（毫秒）
     * @param backoffMultiplier 退避倍数
     * @return 操作结果
     */
    public <T> T executeWithRetry(Supplier<T> operation, int maxRetries, 
                                  long initialDelay, double backoffMultiplier) {
        int attemptCount = 0;
        long currentDelay = initialDelay;
        Exception lastException = null;
        
        while (attemptCount <= maxRetries) {
            try {
                if (attemptCount > 0) {
                    log.info("🔄 重试第{}次...", attemptCount);
                }
                
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                attemptCount++;
                
                // 检查是否需要重试
                if (!shouldRetry(e) || attemptCount > maxRetries) {
                    break;
                }
                
                log.warn("⚠️ 操作失败，{}秒后重试 (第{}/{}次)", 
                        currentDelay / 1000.0, attemptCount, maxRetries);
                
                // 等待后重试（指数退避）
                try {
                    Thread.sleep(currentDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
                
                // 增加延迟时间（指数退避）
                currentDelay = (long) (currentDelay * backoffMultiplier);
            }
        }
        
        // 所有重试都失败了
        log.error("❌ 操作失败，已重试{}次", attemptCount - 1);
        throw new RuntimeException("操作失败，已达最大重试次数", lastException);
    }
    
    /**
     * 判断异常是否应该重试
     */
    private boolean shouldRetry(Exception e) {
        // 网络相关异常 - 应该重试
        if (e instanceof java.net.SocketTimeoutException ||
            e instanceof java.net.ConnectException ||
            e instanceof java.io.IOException) {
            return true;
        }
        
        // API限流异常 - 应该重试
        if (e.getMessage() != null && 
            (e.getMessage().contains("rate limit") || 
             e.getMessage().contains("429") ||
             e.getMessage().contains("too many requests"))) {
            return true;
        }
        
        // 服务暂时不可用 - 应该重试
        if (e.getMessage() != null && 
            (e.getMessage().contains("503") ||
             e.getMessage().contains("temporarily unavailable"))) {
            return true;
        }
        
        // 参数错误、认证错误等 - 不应该重试
        if (e.getMessage() != null &&
            (e.getMessage().contains("400") ||
             e.getMessage().contains("401") ||
             e.getMessage().contains("403") ||
             e.getMessage().contains("invalid"))) {
            return false;
        }
        
        // 默认：对未知异常进行重试
        return true;
    }
    
    /**
     * 执行带重试的AI调用
     * (针对AI API调用的特殊配置)
     */
    public <T> T executeAICallWithRetry(Supplier<T> aiCall) {
        // AI调用通常较慢，增加重试间隔
        return executeWithRetry(aiCall, 3, 2000, 2.0);
    }
    
    /**
     * 执行带重试的数据库操作
     * (针对数据库操作的特殊配置)
     */
    public <T> T executeDBCallWithRetry(Supplier<T> dbCall) {
        // 数据库操作重试间隔较短
        return executeWithRetry(dbCall, 2, 500, 1.5);
    }
}























