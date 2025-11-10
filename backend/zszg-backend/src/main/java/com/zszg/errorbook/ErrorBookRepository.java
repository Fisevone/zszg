package com.zszg.errorbook;

import com.zszg.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ErrorBookRepository extends JpaRepository<ErrorBook, Long> {
    // 使用JOIN FETCH主动加载question，避免懒加载问题
    @Query("SELECT DISTINCT e FROM ErrorBook e LEFT JOIN FETCH e.question WHERE e.user = :user ORDER BY e.createdAt DESC")
    List<ErrorBook> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT DISTINCT e FROM ErrorBook e LEFT JOIN FETCH e.question q WHERE e.user = :user AND q.subject = :subject ORDER BY e.createdAt DESC")
    List<ErrorBook> findByUserAndSubject(@Param("user") User user, @Param("subject") String subject);
    
    List<ErrorBook> findByStatus(String status);
    
    // 统计方法
    long countByUser(User user);
    
    long countByUserAndCorrectionIsNotNull(User user);
    
    long countByUserAndStatus(User user, String status);
    
    long countByStatus(String status);
}
