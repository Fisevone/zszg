package com.zszg.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 查询教师发布的任务
    List<Task> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    
    // 查询班级的任务
    List<Task> findByClassIdOrderByCreatedAtDesc(String classId);
    
    // 查询教师在某个班级的任务
    List<Task> findByTeacherIdAndClassIdOrderByCreatedAtDesc(Long teacherId, String classId);
    
    // 统计班级任务数
    long countByClassId(String classId);
    
    // 统计教师发布的任务数
    long countByTeacherId(Long teacherId);
}





















