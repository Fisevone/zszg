package com.zszg.ai;

import com.zszg.common.ApiResponse;
import com.zszg.service.AsyncAIService;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步任务控制器
 * 
 * 提供异步AI任务的提交和查询接口
 * 
 * 工作流程:
 * 1. 前端提交任务 -> 返回任务ID
 * 2. 前端轮询查询任务状态
 * 3. 任务完成后返回结果
 */
@Slf4j
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncTaskController {
    
    private final AsyncAIService asyncAIService;
    private final UserRepository userRepository;
    
    // 任务缓存 (实际生产环境应使用Redis)
    private final Map<String, TaskInfo> taskCache = new ConcurrentHashMap<>();
    
    /**
     * 提交异步AI分析任务
     */
    @PostMapping("/analyze")
    public ApiResponse<Map<String, String>> submitAnalysisTask(
            @RequestParam String subject,
            @RequestParam String questionContent,
            @RequestParam String correctAnswer,
            @RequestParam String userAnswer,
            @RequestParam String difficulty) {
        
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 提交异步任务
        CompletableFuture<String> future = asyncAIService.analyzeErrorQuestionAsync(
            subject, questionContent, correctAnswer, userAnswer, difficulty
        );
        
        // 保存任务信息
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus("processing");
        taskInfo.setSubmitTime(System.currentTimeMillis());
        taskCache.put(taskId, taskInfo);
        
        // 任务完成时更新状态
        future.thenAccept(result -> {
            taskInfo.setStatus("completed");
            taskInfo.setResult(result);
            taskInfo.setCompleteTime(System.currentTimeMillis());
        }).exceptionally(ex -> {
            taskInfo.setStatus("failed");
            taskInfo.setError(ex.getMessage());
            taskInfo.setCompleteTime(System.currentTimeMillis());
            return null;
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "任务已提交，请使用taskId查询结果");
        
        log.info("✅ 异步分析任务已提交 - taskId: {}", taskId);
        return ApiResponse.success(response);
    }
    
    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<TaskInfo> getTaskStatus(@PathVariable String taskId) {
        TaskInfo taskInfo = taskCache.get(taskId);
        
        if (taskInfo == null) {
            return ApiResponse.error("任务不存在");
        }
        
        return ApiResponse.success(taskInfo);
    }
    
    /**
     * 提交异步思维导图生成任务
     */
    @PostMapping("/mindmap")
    public ApiResponse<Map<String, String>> submitMindMapTask(
            @RequestParam String content,
            @RequestParam String subject,
            @RequestParam(defaultValue = "ERRORBOOK") String type) {
        
        String taskId = UUID.randomUUID().toString();
        
        MindMapService.MindMapType mapType = MindMapService.MindMapType.valueOf(type);
        CompletableFuture<MindMapService.MindMapData> future = 
            asyncAIService.generateMindMapAsync(content, subject, mapType);
        
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus("processing");
        taskInfo.setSubmitTime(System.currentTimeMillis());
        taskCache.put(taskId, taskInfo);
        
        future.thenAccept(result -> {
            taskInfo.setStatus("completed");
            taskInfo.setResult(result);
            taskInfo.setCompleteTime(System.currentTimeMillis());
        }).exceptionally(ex -> {
            taskInfo.setStatus("failed");
            taskInfo.setError(ex.getMessage());
            taskInfo.setCompleteTime(System.currentTimeMillis());
            return null;
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "思维导图生成任务已提交");
        
        log.info("✅ 异步思维导图任务已提交 - taskId: {}", taskId);
        return ApiResponse.success(response);
    }
    
    /**
     * 提交异步预测分析任务
     */
    @PostMapping("/prediction")
    public ApiResponse<Map<String, String>> submitPredictionTask(
            @RequestParam String subject,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String taskId = UUID.randomUUID().toString();
        
        CompletableFuture<PredictionService.PredictionReport> future = 
            asyncAIService.generatePredictionAsync(user, subject);
        
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus("processing");
        taskInfo.setSubmitTime(System.currentTimeMillis());
        taskCache.put(taskId, taskInfo);
        
        future.thenAccept(result -> {
            taskInfo.setStatus("completed");
            taskInfo.setResult(result);
            taskInfo.setCompleteTime(System.currentTimeMillis());
        }).exceptionally(ex -> {
            taskInfo.setStatus("failed");
            taskInfo.setError(ex.getMessage());
            taskInfo.setCompleteTime(System.currentTimeMillis());
            return null;
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "预测分析任务已提交");
        
        log.info("✅ 异步预测分析任务已提交 - taskId: {}", taskId);
        return ApiResponse.success(response);
    }
    
    /**
     * 提交异步拍照搜题任务
     */
    @PostMapping("/photo-search")
    public ApiResponse<Map<String, String>> submitPhotoSearchTask(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam String subject) {
        
        String taskId = UUID.randomUUID().toString();
        
        CompletableFuture<PhotoSearchService.SearchResult> future = 
            asyncAIService.photoSearchAsync(imageFile, subject);
        
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus("processing");
        taskInfo.setSubmitTime(System.currentTimeMillis());
        taskCache.put(taskId, taskInfo);
        
        future.thenAccept(result -> {
            taskInfo.setStatus("completed");
            taskInfo.setResult(result);
            taskInfo.setCompleteTime(System.currentTimeMillis());
        }).exceptionally(ex -> {
            taskInfo.setStatus("failed");
            taskInfo.setError(ex.getMessage());
            taskInfo.setCompleteTime(System.currentTimeMillis());
            return null;
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "拍照搜题任务已提交");
        
        log.info("✅ 异步拍照搜题任务已提交 - taskId: {}", taskId);
        return ApiResponse.success(response);
    }
    
    /**
     * 清理过期任务（定时清理，避免内存泄漏）
     */
    @PostMapping("/clean")
    public ApiResponse<String> cleanExpiredTasks() {
        long now = System.currentTimeMillis();
        long expireTime = 1000 * 60 * 60; // 1小时
        
        int count = 0;
        for (Map.Entry<String, TaskInfo> entry : taskCache.entrySet()) {
            TaskInfo task = entry.getValue();
            if (task.getCompleteTime() != null && 
                (now - task.getCompleteTime()) > expireTime) {
                taskCache.remove(entry.getKey());
                count++;
            }
        }
        
        log.info("✅ 清理过期任务 - 数量: {}", count);
        return ApiResponse.success("已清理" + count + "个过期任务");
    }
    
    /**
     * 任务信息
     */
    public static class TaskInfo {
        private String taskId;
        private String status;  // processing/completed/failed
        private Object result;
        private String error;
        private Long submitTime;
        private Long completeTime;
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public Long getSubmitTime() { return submitTime; }
        public void setSubmitTime(Long submitTime) { this.submitTime = submitTime; }
        
        public Long getCompleteTime() { return completeTime; }
        public void setCompleteTime(Long completeTime) { this.completeTime = completeTime; }
        
        public Long getDuration() {
            if (completeTime != null && submitTime != null) {
                return completeTime - submitTime;
            }
            return null;
        }
    }
}























