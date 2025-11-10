package com.zszg.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeacherPushRepository extends JpaRepository<TeacherPush, Long> {
    List<TeacherPush> findByClassIdOrderByCreatedAtDesc(Long classId);
    List<TeacherPush> findByClassId(Long classId);
    long countByClassId(Long classId);
    
    @Modifying
    @Transactional
    void deleteByClassId(Long classId);
}













