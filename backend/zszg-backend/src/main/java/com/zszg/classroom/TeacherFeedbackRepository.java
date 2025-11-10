package com.zszg.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeacherFeedbackRepository extends JpaRepository<TeacherFeedback, Long> {
    List<TeacherFeedback> findByErrorBookId(Long errorBookId);
    List<TeacherFeedback> findByStudentId(Long studentId);
    
    @Modifying
    @Transactional
    void deleteByTeacherId(Long teacherId);
}













