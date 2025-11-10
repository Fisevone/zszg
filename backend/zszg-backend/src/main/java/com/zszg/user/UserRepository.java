package com.zszg.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByRole(String role);
    
    List<User> findByClassId(String classId);
    
    List<User> findByStatus(String status);
    
    // 统计方法
    long countByRole(String role);
}
