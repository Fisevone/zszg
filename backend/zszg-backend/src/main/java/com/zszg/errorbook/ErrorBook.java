package com.zszg.errorbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zszg.question.Question;
import com.zszg.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_error_book")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ErrorBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    private String errorReason;

    @Lob
    private String correction;

    @Column(length = 255)
    private String tags; // 标签，逗号分隔
    
    @Column(columnDefinition = "JSON")
    private String images; // JSON 数组存储图片URL

    @Column(length = 20)
    private String status; // PRIVATE / SHARED

    @Builder.Default
    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }
}


