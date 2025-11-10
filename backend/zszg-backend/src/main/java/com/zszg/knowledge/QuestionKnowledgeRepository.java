package com.zszg.knowledge;

import com.zszg.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionKnowledgeRepository extends JpaRepository<QuestionKnowledge, Long> {
    List<QuestionKnowledge> findByKnowledgeId(Long knowledgeId);
    List<QuestionKnowledge> findByQuestionId(Long questionId);
    Optional<QuestionKnowledge> findByQuestionIdAndKnowledgeId(Long questionId, Long knowledgeId);
    boolean existsByQuestionAndKnowledge(Question question, Knowledge knowledge);
}


