package com.zszg.task;

import com.zszg.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能任务推送控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务（教师）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Task> createTask(@RequestBody Map<String, Object> request) {
        
        Long teacherId = ((Number) request.get("teacherId")).longValue();
        String teacherName = (String) request.get("teacherName");
        String classId = (String) request.get("classId");
        String className = (String) request.get("className");
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Boolean useAI = (Boolean) request.getOrDefault("useAI", true);

        log.info("📝 教师 {} 创建任务：班级={}, 标题={}", teacherName, className, title);

        Task task = taskService.createTask(teacherId, teacherName, classId, className, title, content, useAI);
        return ApiResponse.ok(task);
    }

    /**
     * AI智能解析任务内容（预览）
     */
    @PostMapping("/parse-preview")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Map<String, Object>> parseTaskPreview(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        log.info("🧠 AI解析任务内容预览：{}", content.substring(0, Math.min(50, content.length())));
        
        Map<String, Object> result = taskService.previewTaskParse(content);
        return ApiResponse.ok(result);
    }

    /**
     * 发布任务（下发给学生）
     */
    @PostMapping("/{taskId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> publishTask(@PathVariable Long taskId) {
        log.info("📤 发布任务：ID={}", taskId);
        taskService.publishTask(taskId);
        return ApiResponse.ok(null);
    }

    /**
     * 获取教师的任务列表
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getTeacherTasks(@PathVariable Long teacherId) {
        List<Map<String, Object>> tasks = taskService.getTeacherTasks(teacherId);
        return ApiResponse.ok(tasks);
    }

    /**
     * 获取学生的任务列表
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getStudentTasks(@PathVariable Long studentId) {
        List<Map<String, Object>> tasks = taskService.getStudentTasks(studentId);
        return ApiResponse.ok(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getTaskDetail(@PathVariable Long taskId) {
        Map<String, Object> task = taskService.getTaskDetail(taskId);
        return ApiResponse.ok(task);
    }

    /**
     * 学生完成任务
     */
    @PostMapping("/student/{studentTaskId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> completeTask(
            @PathVariable Long studentTaskId,
            @RequestBody Map<String, Object> request) {
        
        Long studentId = ((Number) request.get("studentId")).longValue();
        String notes = (String) request.get("notes");
        
        log.info("✅ 学生 {} 完成任务 {}", studentId, studentTaskId);
        taskService.completeTask(studentTaskId, studentId, notes);
        return ApiResponse.ok(null);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId, @RequestParam Long teacherId) {
        log.info("🗑️ 删除任务：ID={}, 教师ID={}", taskId, teacherId);
        taskService.deleteTask(taskId, teacherId);
        return ApiResponse.ok(null);
    }
}

