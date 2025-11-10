package com.zszg.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceItem, Long> {
    List<ResourceItem> findByApprovedTrueOrderByCreatedAtDesc();
    
    List<ResourceItem> findByApprovedFalseOrderByCreatedAtDesc();
    
    List<ResourceItem> findByTypeOrderByCreatedAtDesc(String type);
    
    List<ResourceItem> findBySubjectAndApprovedTrueOrderByCreatedAtDesc(String subject);
    
    List<ResourceItem> findByTypeAndApprovedTrueOrderByCreatedAtDesc(String type);
    
    List<ResourceItem> findByTitleContainingAndApprovedTrue(String keyword);
}
