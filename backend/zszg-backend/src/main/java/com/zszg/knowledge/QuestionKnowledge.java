package com.zszg.knowledge;

import com.zszg.question.Question;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_question_knowledge")
public class QuestionKnowledge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_id", nullable = false)
    private Knowledge knowledge;

    @Builder.Default
    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}


