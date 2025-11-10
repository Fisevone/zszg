package com.zszg.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    private String realName;
    private String phone;
    private String email;

    @Column(nullable = false, length = 20)
    private String role; // ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN

    @Column(length = 20)
    private String status; // ACTIVE / FROZEN

    private String classId;
    private String grade;

    @Builder.Default
    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}



