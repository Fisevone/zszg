package com.zszg.service;

import com.zszg.ai.GLMService;
import com.zszg.ai.MindMapService;
import com.zszg.ai.PredictionService;
import com.zszg.ai.PhotoSearchService;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * 异步AI服务
 * 
 * 功能:
 * 1. AI分析异步执行
 * 2. 知识点提取异步执行
 * 3. 思维导图生成异步执行
 * 4. 预测分析异步执行
 * 5. 返回CompletableFuture供前端轮询或WebSocket推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAIService {
    
    private final GLMService glmService;
    private final MindMapService mindMapService;
    private final PredictionService predictionService;
    private final PhotoSearchService photoSearchService;
    private final CacheService cacheService;
    
    /**
     * 异步AI分析
     * 
     * @return CompletableFuture<String> 分析结果的Future
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<String> analyzeErrorQuestionAsync(
            String subject, String questionContent, 
            String correctAnswer, String userAnswer, String difficulty) {
        
        log.info("🚀 开始异步AI分析 - 学科: {}", subject);
        
        try {
            String analysis = glmService.analyzeErrorQuestion(
                subject, questionContent, correctAnswer, userAnswer, difficulty
            );
            
            log.info("✅ 异步AI分析完成 - 结果长度: {} 字符", analysis.length());
            return CompletableFuture.completedFuture(analysis);
            
        } catch (Exception e) {
            log.error("❌ 异步AI分析失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步提取知识点
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<Object> extractKnowledgeAsync(String subject, String questionContent) {
        log.info("🚀 开始异步提取知识点 - 学科: {}", subject);
        
        try {
            Object knowledge = cacheService.getOrExtractKnowledge(
                subject, questionContent,
                () -> glmService.extractKnowledgePoints(subject, questionContent)
            );
            
            log.info("✅ 异步知识点提取完成");
            return CompletableFuture.completedFuture(knowledge);
            
        } catch (Exception e) {
            log.error("❌ 异步知识点提取失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步生成思维导图
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<MindMapService.MindMapData> generateMindMapAsync(
            String content, String subject, MindMapService.MindMapType type) {
        
        log.info("🚀 开始异步生成思维导图 - 学科: {}, 类型: {}", subject, type);
        
        try {
            MindMapService.MindMapData mindMap = 
                (MindMapService.MindMapData) cacheService.getOrGenerateMindMap(
                    content, subject,
                    () -> mindMapService.generateMindMap(content, subject, type)
                );
            
            log.info("✅ 异步思维导图生成完成 - 节点数: {}", mindMap.getNodes().size());
            return CompletableFuture.completedFuture(mindMap);
            
        } catch (Exception e) {
            log.error("❌ 异步思维导图生成失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步生成预测报告
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<PredictionService.PredictionReport> generatePredictionAsync(
            User user, String subject) {
        
        log.info("🚀 开始异步生成预测报告 - 用户: {}, 学科: {}", user.getId(), subject);
        
        try {
            PredictionService.PredictionReport report = 
                (PredictionService.PredictionReport) cacheService.getOrGeneratePrediction(
                    user.getId(), subject,
                    () -> predictionService.predictWeakness(user, subject)
                );
            
            log.info("✅ 异步预测报告生成完成 - 预测数: {}", 
                    report.getPredictions().size());
            return CompletableFuture.completedFuture(report);
            
        } catch (Exception e) {
            log.error("❌ 异步预测报告生成失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步拍照搜题
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<PhotoSearchService.SearchResult> photoSearchAsync(
            MultipartFile imageFile, String subject) {
        
        log.info("🚀 开始异步拍照搜题 - 学科: {}, 文件: {}", 
                subject, imageFile.getOriginalFilename());
        
        try {
            PhotoSearchService.SearchResult result = 
                photoSearchService.searchQuestion(imageFile, subject);
            
            log.info("✅ 异步拍照搜题完成 - 成功: {}", result.isSuccess());
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("❌ 异步拍照搜题失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步生成学习报告
     */
    @Async("dataProcessExecutor")
    public CompletableFuture<String> generateLearningReportAsync(
            String studentName, String subject, 
            java.util.Map<String, Long> subjectStats,
            java.util.Map<String, Long> difficultyStats,
            long totalErrors, long correctedCount) {
        
        log.info("🚀 开始异步生成学习报告 - 学生: {}", studentName);
        
        try {
            String report = glmService.generateLearningReport(
                studentName, subjectStats, difficultyStats, totalErrors, correctedCount
            );
            
            log.info("✅ 异步学习报告生成完成 - 长度: {} 字符", report.length());
            return CompletableFuture.completedFuture(report);
            
        } catch (Exception e) {
            log.error("❌ 异步学习报告生成失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 批量异步分析（并行执行多个分析）
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<java.util.Map<String, String>> batchAnalyzeAsync(
            java.util.List<AnalysisRequest> requests) {
        
        log.info("🚀 开始批量异步分析 - 数量: {}", requests.size());
        
        try {
            java.util.Map<String, String> results = new java.util.HashMap<>();
            
            // 并行执行所有分析
            java.util.List<CompletableFuture<java.util.Map.Entry<String, String>>> futures = 
                requests.stream()
                    .map(req -> CompletableFuture.supplyAsync(() -> {
                        String analysis = glmService.analyzeErrorQuestion(
                            req.subject, req.questionContent, 
                            req.correctAnswer, req.userAnswer, req.difficulty
                        );
                        return java.util.Map.entry(req.id, analysis);
                    }))
                    .toList();
            
            // 等待所有完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 收集结果
            for (CompletableFuture<java.util.Map.Entry<String, String>> future : futures) {
                java.util.Map.Entry<String, String> entry = future.get();
                results.put(entry.getKey(), entry.getValue());
            }
            
            log.info("✅ 批量异步分析完成 - 成功数: {}", results.size());
            return CompletableFuture.completedFuture(results);
            
        } catch (Exception e) {
            log.error("❌ 批量异步分析失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 分析请求
     */
    public static class AnalysisRequest {
        public String id;
        public String subject;
        public String questionContent;
        public String correctAnswer;
        public String userAnswer;
        public String difficulty;
        
        public AnalysisRequest(String id, String subject, String questionContent,
                              String correctAnswer, String userAnswer, String difficulty) {
            this.id = id;
            this.subject = subject;
            this.questionContent = questionContent;
            this.correctAnswer = correctAnswer;
            this.userAnswer = userAnswer;
            this.difficulty = difficulty;
        }
    }
}























