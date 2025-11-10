package com.zszg.sharepool;

import com.zszg.errorbook.ErrorBook;
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
@Table(name = "t_share_pool")
public class SharePool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "error_book_id", nullable = false)
    private ErrorBook errorBook;

    @Column(length = 20)
    private String scope; // CLASS / GRADE / SCHOOL

    @Builder.Default
    private Boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;

    @Builder.Default
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;

    @Builder.Default
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer favorites = 0;

    @Builder.Default
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0; // 浏览次数

    @Column(length = 128)
    private String tags;

    @Builder.Default
    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

