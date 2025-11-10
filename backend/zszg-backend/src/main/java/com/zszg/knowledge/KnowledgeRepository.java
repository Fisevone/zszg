package com.zszg.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    List<Knowledge> findBySubject(String subject);
    
    List<Knowledge> findBySubjectAndParentIsNull(String subject);
    
    List<Knowledge> findByParent(Knowledge parent);
    
    List<Knowledge> findByTitleContaining(String keyword);
    
    List<Knowledge> findBySubjectAndTitleContainingIgnoreCase(String subject, String keyword);
}
