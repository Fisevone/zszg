package com.zszg.sharepool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SharePoolRepository extends JpaRepository<SharePool, Long> {
    List<SharePool> findByApprovedTrueOrderByCreatedAtDesc();
    
    List<SharePool> findByApprovedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT s FROM SharePool s WHERE s.approved = true AND s.scope = :scope ORDER BY s.createdAt DESC")
    List<SharePool> findByScope(@Param("scope") String scope);
    
    @Query("SELECT s FROM SharePool s JOIN s.errorBook e JOIN e.question q " +
           "WHERE s.approved = true AND q.subject = :subject ORDER BY s.createdAt DESC")
    List<SharePool> findBySubject(@Param("subject") String subject);
    
    /**
     * 根据错题ID查找共享记录（用于检查是否已分享）
     */
    @Query("SELECT s FROM SharePool s WHERE s.errorBook.id = :errorBookId")
    List<SharePool> findByErrorBookId(@Param("errorBookId") Long errorBookId);
}

