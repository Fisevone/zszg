package com.zszg.task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "teacher_name")
    private String teacherName;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "class_name")
    private String className;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // AI解析结果
    @Column(name = "task_type")
    private String taskType; // 学习任务、活动、考试等

    @Column(name = "priority")
    private String priority; // 普通、重要、紧急

    @Column(name = "deadline")
    private LocalDateTime deadline; // 截止时间

    @Column(name = "location")
    private String location; // 地点

    @Column(name = "participants")
    private String participants; // 参与人群

    @Column(name = "quantity_requirement")
    private String quantityRequirement; // 数量要求

    @Column(name = "important_reminders", columnDefinition = "TEXT")
    private String importantReminders; // 重要提醒

    @Column(name = "smart_tags", columnDefinition = "TEXT")
    private String smartTags; // 智能标签（JSON格式）

    @Column(name = "parsed_tasks", columnDefinition = "TEXT")
    private String parsedTasks; // AI拆解的子任务（JSON格式）

    @Column(name = "ai_notes", columnDefinition = "TEXT")
    private String aiNotes; // AI备注

    @Column(name = "status")
    private String status; // 待下发、已下发、已完成

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "待下发";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}





















