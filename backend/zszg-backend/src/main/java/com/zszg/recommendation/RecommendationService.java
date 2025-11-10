package com.zszg.recommendation;

import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.knowledge.QuestionKnowledge;
import com.zszg.knowledge.QuestionKnowledgeRepository;
import com.zszg.question.Question;
import com.zszg.question.QuestionRepository;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final ErrorBookRepository errorBookRepository;
    private final QuestionRepository questionRepository;
    private final QuestionKnowledgeRepository questionKnowledgeRepository;

    /**
     * 获取个性化推荐题目
     * 基于用户的错题历史，识别薄弱知识点，推荐相关练习题
     */
    public List<Question> getRecommendedQuestions(User user, int limit) {
        // 1. 获取用户的错题历史
        List<ErrorBook> userErrors = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        if (userErrors.isEmpty()) {
            // 如果没有错题，返回随机题目
            return questionRepository.findAll().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 2. 统计错题涉及的知识点频率
        Map<Long, Long> knowledgeErrorCount = new HashMap<>();
        Set<Long> answeredQuestionIds = new HashSet<>();
        
        for (ErrorBook errorBook : userErrors) {
            Question question = errorBook.getQuestion();
            answeredQuestionIds.add(question.getId());
            
            // 获取题目关联的知识点
            List<QuestionKnowledge> qks = questionKnowledgeRepository.findByQuestionId(question.getId());
            for (QuestionKnowledge qk : qks) {
                Long knowledgeId = qk.getKnowledge().getId();
                knowledgeErrorCount.put(knowledgeId, knowledgeErrorCount.getOrDefault(knowledgeId, 0L) + 1);
            }
        }

        // 3. 按错误次数排序，找出薄弱知识点（前5个）
        List<Long> weakKnowledgeIds = knowledgeErrorCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 4. 查找这些薄弱知识点相关的题目（排除已做过的）
        List<Question> recommendations = new ArrayList<>();
        for (Long knowledgeId : weakKnowledgeIds) {
            List<QuestionKnowledge> qks = questionKnowledgeRepository.findByKnowledgeId(knowledgeId);
            for (QuestionKnowledge qk : qks) {
                Question q = qk.getQuestion();
                if (!answeredQuestionIds.contains(q.getId()) && !recommendations.contains(q)) {
                    recommendations.add(q);
                    if (recommendations.size() >= limit) {
                        return recommendations;
                    }
                }
            }
        }

        // 5. 如果推荐题目不足，补充随机题目
        if (recommendations.size() < limit) {
            List<Question> allQuestions = questionRepository.findAll();
            for (Question q : allQuestions) {
                if (!answeredQuestionIds.contains(q.getId()) && !recommendations.contains(q)) {
                    recommendations.add(q);
                    if (recommendations.size() >= limit) {
                        break;
                    }
                }
            }
        }

        return recommendations;
    }

    /**
     * 根据知识点获取练习题
     */
    public List<Question> getQuestionsByKnowledge(Long knowledgeId, int limit) {
        List<QuestionKnowledge> qks = questionKnowledgeRepository.findByKnowledgeId(knowledgeId);
        return qks.stream()
                .map(QuestionKnowledge::getQuestion)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据学科推荐题目
     */
    public List<Question> getQuestionsBySubject(User user, String subject, int limit) {
        // 获取用户已做过的题目
        Set<Long> answeredQuestionIds = errorBookRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(eb -> eb.getQuestion().getId())
                .collect(Collectors.toSet());

        // 查找该学科的题目（排除已做过的）
        return questionRepository.findAll().stream()
                .filter(q -> subject.equals(q.getSubject()))
                .filter(q -> !answeredQuestionIds.contains(q.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}


