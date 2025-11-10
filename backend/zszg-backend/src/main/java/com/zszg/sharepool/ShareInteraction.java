package com.zszg.sharepool;

import com.zszg.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * 共享池用户交互记录（点赞、收藏）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_share_interaction", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "share_pool_id", "interaction_type"}))
public class ShareInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_pool_id", nullable = false)
    private SharePool sharePool;

    @Column(name = "interaction_type", length = 20, nullable = false)
    private String interactionType; // LIKE / FAVORITE

    @Builder.Default
    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}






