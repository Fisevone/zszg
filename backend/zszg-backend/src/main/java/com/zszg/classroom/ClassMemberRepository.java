package com.zszg.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {
    List<ClassMember> findByClassId(Long classId);
    List<ClassMember> findByStudentId(Long studentId);
    Optional<ClassMember> findByClassIdAndStudentId(Long classId, Long studentId);
    long countByClassId(Long classId);
    
    @Modifying
    @Transactional
    void deleteByClassId(Long classId);
}













