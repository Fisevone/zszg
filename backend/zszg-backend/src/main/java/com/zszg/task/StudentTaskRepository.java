package com.zszg.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTaskRepository extends JpaRepository<StudentTask, Long> {
    // 查询学生的所有任务
    List<StudentTask> findByStudentIdOrderByReceivedAtDesc(Long studentId);
    
    // 查询学生的未完成任务
    List<StudentTask> findByStudentIdAndIsCompletedOrderByReceivedAtDesc(Long studentId, Boolean isCompleted);
    
    // 查询某个任务的所有学生完成情况
    List<StudentTask> findByTaskId(Long taskId);
    
    // 查询特定学生的特定任务
    Optional<StudentTask> findByTaskIdAndStudentId(Long taskId, Long studentId);
    
    // 统计学生的任务数
    long countByStudentId(Long studentId);
    
    // 统计学生的已完成任务数
    long countByStudentIdAndIsCompleted(Long studentId, Boolean isCompleted);
}





















