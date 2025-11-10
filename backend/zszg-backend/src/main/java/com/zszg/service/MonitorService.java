package com.zszg.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 系统监控服务
 * 
 * 功能:
 * 1. 监控系统资源使用情况
 * 2. 记录API调用统计
 * 3. 性能指标收集
 * 4. 异常统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {
    
    private final CacheService cacheService;
    
    // API调用统计
    private final ConcurrentLinkedQueue<APICallRecord> apiCallRecords = 
            new ConcurrentLinkedQueue<>();
    
    // 异常记录
    private final ConcurrentLinkedQueue<ErrorRecord> errorRecords = 
            new ConcurrentLinkedQueue<>();
    
    // 最大记录数（避免内存溢出）
    private static final int MAX_RECORDS = 1000;
    
    /**
     * 记录API调用
     */
    public void recordAPICall(String apiPath, long duration, boolean success) {
        APICallRecord record = new APICallRecord();
        record.setApiPath(apiPath);
        record.setDuration(duration);
        record.setSuccess(success);
        record.setTimestamp(LocalDateTime.now());
        
        apiCallRecords.offer(record);
        
        // 清理过多的记录
        while (apiCallRecords.size() > MAX_RECORDS) {
            apiCallRecords.poll();
        }
        
        // 慢API警告
        if (duration > 3000) {
            log.warn("⚠️ 慢API警告 - 路径: {}, 耗时: {}ms", apiPath, duration);
        }
    }
    
    /**
     * 记录异常
     */
    public void recordError(String errorType, String message, String stackTrace) {
        ErrorRecord record = new ErrorRecord();
        record.setErrorType(errorType);
        record.setMessage(message);
        record.setStackTrace(stackTrace);
        record.setTimestamp(LocalDateTime.now());
        
        errorRecords.offer(record);
        
        // 清理过多的记录
        while (errorRecords.size() > MAX_RECORDS) {
            errorRecords.poll();
        }
    }
    
    /**
     * 获取系统监控数据
     */
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        
        // JVM内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / 1024 / 1024;
        
        metrics.setUsedMemoryMB(usedMemory);
        metrics.setMaxMemoryMB(maxMemory);
        metrics.setMemoryUsagePercent((double) usedMemory / maxMemory * 100);
        
        // 线程信息
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        metrics.setThreadCount(threadBean.getThreadCount());
        metrics.setPeakThreadCount(threadBean.getPeakThreadCount());
        
        // API调用统计
        metrics.setTotalAPICalls(apiCallRecords.size());
        long successCalls = apiCallRecords.stream()
                .filter(APICallRecord::isSuccess).count();
        metrics.setSuccessAPICalls(successCalls);
        metrics.setFailedAPICalls(apiCallRecords.size() - successCalls);
        
        // 平均响应时间
        double avgDuration = apiCallRecords.stream()
                .mapToLong(APICallRecord::getDuration)
                .average()
                .orElse(0);
        metrics.setAvgResponseTimeMS((long) avgDuration);
        
        // 缓存统计
        if (cacheService != null) {
            CacheService.CacheStats cacheStats = cacheService.getCacheStats();
            metrics.setCacheAvailable(cacheStats.isAvailable());
            metrics.setTotalCachedItems(cacheStats.getTotalCount());
        }
        
        // 错误统计
        metrics.setTotalErrors(errorRecords.size());
        
        return metrics;
    }
    
    /**
     * 获取最近的API调用记录
     */
    public List<APICallRecord> getRecentAPICalls(int limit) {
        List<APICallRecord> records = new ArrayList<>(apiCallRecords);
        if (records.size() > limit) {
            return records.subList(records.size() - limit, records.size());
        }
        return records;
    }
    
    /**
     * 获取最近的错误记录
     */
    public List<ErrorRecord> getRecentErrors(int limit) {
        List<ErrorRecord> records = new ArrayList<>(errorRecords);
        if (records.size() > limit) {
            return records.subList(records.size() - limit, records.size());
        }
        return records;
    }
    
    /**
     * 定时打印系统状态（每小时）
     */
    @Scheduled(fixedRate = 3600000)
    public void printSystemStatus() {
        SystemMetrics metrics = getSystemMetrics();
        
        log.info("=".repeat(60));
        log.info("📊 系统运行状态报告");
        log.info("=".repeat(60));
        log.info("内存使用: {}/{} MB ({}%)", 
                metrics.getUsedMemoryMB(), metrics.getMaxMemoryMB(), 
                String.format("%.1f", metrics.getMemoryUsagePercent()));
        log.info("线程数: {} (峰值: {})", 
                metrics.getThreadCount(), metrics.getPeakThreadCount());
        log.info("API调用: 总计={}, 成功={}, 失败={}, 平均耗时={}ms",
                metrics.getTotalAPICalls(), metrics.getSuccessAPICalls(), 
                metrics.getFailedAPICalls(), metrics.getAvgResponseTimeMS());
        log.info("缓存状态: {}, 缓存项数: {}",
                metrics.isCacheAvailable() ? "正常" : "不可用",
                metrics.getTotalCachedItems());
        log.info("错误数: {}", metrics.getTotalErrors());
        log.info("=".repeat(60));
    }
    
    /**
     * 清理旧记录
     */
    public void clearOldRecords() {
        apiCallRecords.clear();
        errorRecords.clear();
        log.info("✅ 监控记录已清理");
    }
    
    // ======== 数据类 ========
    
    @Data
    public static class APICallRecord {
        private String apiPath;
        private long duration;
        private boolean success;
        private LocalDateTime timestamp;
    }
    
    @Data
    public static class ErrorRecord {
        private String errorType;
        private String message;
        private String stackTrace;
        private LocalDateTime timestamp;
    }
    
    @Data
    public static class SystemMetrics {
        private long usedMemoryMB;
        private long maxMemoryMB;
        private double memoryUsagePercent;
        private int threadCount;
        private int peakThreadCount;
        private long totalAPICalls;
        private long successAPICalls;
        private long failedAPICalls;
        private long avgResponseTimeMS;
        private boolean cacheAvailable;
        private long totalCachedItems;
        private long totalErrors;
    }
}























