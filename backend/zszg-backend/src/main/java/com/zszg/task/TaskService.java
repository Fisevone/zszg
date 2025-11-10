package com.zszg.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentTaskRepository studentTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskNLPService nlpService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建任务（教师发布）
     */
    @Transactional
    public Task createTask(Long teacherId, String teacherName, String classId, 
                          String className, String title, String content, boolean useAI) {
        log.info("📝 创建任务：教师={}, 班级={}, 标题={}, 使用AI={}", 
                 teacherName, className, title, useAI);

        Task task = Task.builder()
                .teacherId(teacherId)
                .teacherName(teacherName)
                .classId(classId)
                .className(className)
                .title(title)
                .content(content)
                .status("待下发")
                .build();

        // 如果启用AI解析
        if (useAI) {
            try {
                TaskNLPService.TaskParseResult parseResult = nlpService.parseTaskContent(content);
                
                // 如果没有指定标题，使用AI生成的标题
                if (title == null || title.trim().isEmpty()) {
                    task.setTitle(parseResult.getTitle());
                }
                
                task.setTaskType(parseResult.getTaskType());
                task.setPriority(parseResult.getPriority());
                task.setDeadline(parseResult.getDeadline());
                task.setLocation(parseResult.getLocation());
                task.setParticipants(parseResult.getParticipants());
                task.setQuantityRequirement(parseResult.getQuantityRequirement());
                task.setAiNotes(parseResult.getAiNotes());
                
                // 转换为JSON字符串存储
                task.setSmartTags(objectMapper.writeValueAsString(parseResult.getSmartTags()));
                task.setParsedTasks(objectMapper.writeValueAsString(parseResult.getSubTasks()));
                task.setImportantReminders(objectMapper.writeValueAsString(parseResult.getImportantReminders()));
                
                log.info("✅ AI解析完成：{}", parseResult);
            } catch (Exception e) {
                log.error("❌ AI解析失败", e);
                task.setAiNotes("AI解析失败：" + e.getMessage());
            }
        }

        task = taskRepository.save(task);
        log.info("✅ 任务创建成功：ID={}", task.getId());
        
        return task;
    }

    /**
     * 发布任务给学生（下发任务）
     */
    @Transactional
    public void publishTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if ("已下发".equals(task.getStatus())) {
            throw new RuntimeException("任务已经下发，请勿重复操作");
        }

        // 查找班级的所有学生
        List<User> students = userRepository.findByClassId(task.getClassId());
        
        if (students.isEmpty()) {
            throw new RuntimeException("班级中没有学生");
        }

        // 为每个学生创建任务记录
        for (User student : students) {
            StudentTask studentTask = StudentTask.builder()
                    .taskId(taskId)
                    .studentId(student.getId())
                    .studentName(student.getRealName())
                    .isCompleted(false)
                    .build();
            studentTaskRepository.save(studentTask);
        }

        // 更新任务状态
        task.setStatus("已下发");
        taskRepository.save(task);

        log.info("✅ 任务已发布给 {} 名学生", students.size());
    }

    /**
     * 获取教师发布的所有任务
     */
    public List<Map<String, Object>> getTeacherTasks(Long teacherId) {
        List<Task> tasks = taskRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        return tasks.stream().map(this::convertTaskToMap).collect(Collectors.toList());
    }

    /**
     * 获取学生的任务列表
     */
    public List<Map<String, Object>> getStudentTasks(Long studentId) {
        List<StudentTask> studentTasks = studentTaskRepository.findByStudentIdOrderByReceivedAtDesc(studentId);
        
        return studentTasks.stream().map(st -> {
            Task task = taskRepository.findById(st.getTaskId()).orElse(null);
            if (task == null) return null;
            
            Map<String, Object> map = convertTaskToMap(task);
            map.put("studentTaskId", st.getId());
            map.put("isCompleted", st.getIsCompleted());
            map.put("completedAt", st.getCompletedAt());
            map.put("studentNotes", st.getNotes());
            map.put("receivedAt", st.getReceivedAt());
            
            return map;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 学生完成任务
     */
    @Transactional
    public void completeTask(Long studentTaskId, Long studentId, String notes) {
        StudentTask studentTask = studentTaskRepository.findById(studentTaskId)
                .orElseThrow(() -> new RuntimeException("学生任务不存在"));

        if (!studentTask.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此任务");
        }

        studentTask.setIsCompleted(true);
        studentTask.setCompletedAt(LocalDateTime.now());
        studentTask.setNotes(notes);
        studentTaskRepository.save(studentTask);

        log.info("✅ 学生 {} 完成任务 {}", studentId, studentTaskId);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long taskId, Long teacherId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权删除此任务");
        }

        // 删除学生任务记录
        studentTaskRepository.deleteAll(studentTaskRepository.findByTaskId(taskId));
        
        // 删除任务
        taskRepository.delete(task);
        
        log.info("✅ 任务 {} 已删除", taskId);
    }

    /**
     * 获取任务详情
     */
    public Map<String, Object> getTaskDetail(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        Map<String, Object> result = convertTaskToMap(task);
        
        // 添加完成情况统计
        List<StudentTask> studentTasks = studentTaskRepository.findByTaskId(taskId);
        long completedCount = studentTasks.stream().filter(StudentTask::getIsCompleted).count();
        
        result.put("totalStudents", studentTasks.size());
        result.put("completedStudents", completedCount);
        result.put("completionRate", studentTasks.isEmpty() ? 0 : 
                    (double) completedCount / studentTasks.size() * 100);
        
        return result;
    }

    /**
     * AI智能解析任务内容（预览）
     */
    public Map<String, Object> previewTaskParse(String content) {
        TaskNLPService.TaskParseResult result = nlpService.parseTaskContent(content);
        
        Map<String, Object> map = new HashMap<>();
        map.put("title", result.getTitle());
        map.put("taskType", result.getTaskType());
        map.put("deadline", result.getDeadline());
        map.put("priority", result.getPriority());
        map.put("location", result.getLocation());
        map.put("participants", result.getParticipants());
        map.put("quantityRequirement", result.getQuantityRequirement());
        map.put("smartTags", result.getSmartTags());
        map.put("subTasks", result.getSubTasks());
        map.put("importantReminders", result.getImportantReminders());
        map.put("aiNotes", result.getAiNotes());
        
        return map;
    }

    /**
     * 转换Task为Map
     */
    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("teacherId", task.getTeacherId());
        map.put("teacherName", task.getTeacherName());
        map.put("classId", task.getClassId());
        map.put("className", task.getClassName());
        map.put("title", task.getTitle());
        map.put("content", task.getContent());
        map.put("taskType", task.getTaskType());
        map.put("priority", task.getPriority());
        map.put("deadline", task.getDeadline());
        map.put("location", task.getLocation());
        map.put("participants", task.getParticipants());
        map.put("quantityRequirement", task.getQuantityRequirement());
        map.put("status", task.getStatus());
        map.put("aiNotes", task.getAiNotes());
        map.put("createdAt", task.getCreatedAt());
        map.put("updatedAt", task.getUpdatedAt());
        
        // 解析JSON字段
        try {
            if (task.getSmartTags() != null) {
                map.put("smartTags", objectMapper.readValue(task.getSmartTags(), List.class));
            }
            if (task.getParsedTasks() != null) {
                map.put("parsedTasks", objectMapper.readValue(task.getParsedTasks(), List.class));
            }
            if (task.getImportantReminders() != null) {
                map.put("importantReminders", objectMapper.readValue(task.getImportantReminders(), List.class));
            }
        } catch (Exception e) {
            log.warn("解析JSON字段失败", e);
        }
        
        return map;
    }
}





















