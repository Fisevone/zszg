package com.zszg.stats;

import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.knowledge.Knowledge;
import com.zszg.knowledge.KnowledgeRepository;
import com.zszg.knowledge.QuestionKnowledge;
import com.zszg.knowledge.QuestionKnowledgeRepository;
import com.zszg.question.QuestionRepository;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ErrorBookRepository errorBookRepository;
    private final QuestionRepository questionRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final QuestionKnowledgeRepository questionKnowledgeRepository;
    private final UserRepository userRepository;

    /**
     * 获取学生个人统计数据
     */
    public Map<String, Object> getUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalErrors = errorBookRepository.countByUser(user);
        long correctedErrors = errorBookRepository.countByUserAndCorrectionIsNotNull(user);
        long sharedErrors = errorBookRepository.countByUserAndStatus(user, "SHARED");
        
        stats.put("totalErrors", totalErrors);
        stats.put("correctedErrors", correctedErrors);
        stats.put("sharedErrors", sharedErrors);
        stats.put("correctionRate", totalErrors > 0 ? (correctedErrors * 100 / totalErrors) : 0);
        
        return stats;
    }

    /**
     * 获取教师数据面板统计
     */
    public Map<String, Object> getTeacherStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalStudents = userRepository.countByRole("ROLE_STUDENT");
        long totalErrors = errorBookRepository.count();
        long totalQuestions = questionRepository.count();
        long totalKnowledgePoints = knowledgeRepository.count();
        long totalShared = errorBookRepository.countByStatus("SHARED");
        
        stats.put("totalStudents", totalStudents);
        stats.put("totalErrors", totalErrors);
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalKnowledgePoints", totalKnowledgePoints);
        stats.put("totalShared", totalShared);
        
        return stats;
    }

    /**
     * 获取学科错题统计
     */
    public Map<String, Long> getSubjectStats(User user) {
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        return errorBooks.stream()
                .collect(Collectors.groupingBy(
                        eb -> eb.getQuestion().getSubject(),
                        Collectors.counting()
                ));
    }

    /**
     * 获取难度分布统计
     */
    public Map<String, Long> getDifficultyStats(User user) {
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        return errorBooks.stream()
                .filter(eb -> eb.getQuestion().getDifficulty() != null)
                .collect(Collectors.groupingBy(
                        eb -> eb.getQuestion().getDifficulty(),
                        Collectors.counting()
                ));
    }

    /**
     * 获取知识点热度统计
     */
    public Map<String, Object> getKnowledgeHotness() {
        Map<String, Object> result = new HashMap<>();
        
        List<ErrorBook> allErrors = errorBookRepository.findAll();
        Map<Long, Long> knowledgeErrorCount = new HashMap<>();
        
        for (ErrorBook errorBook : allErrors) {
            List<QuestionKnowledge> qks = questionKnowledgeRepository.findByQuestionId(errorBook.getQuestion().getId());
            for (QuestionKnowledge qk : qks) {
                Long knowledgeId = qk.getKnowledge().getId();
                knowledgeErrorCount.put(knowledgeId, knowledgeErrorCount.getOrDefault(knowledgeId, 0L) + 1);
            }
        }
        
        // 按热度排序，取前10个
        List<Map<String, Object>> hotKnowledgeList = knowledgeErrorCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    Optional<Knowledge> knowledge = knowledgeRepository.findById(entry.getKey());
                    item.put("knowledgeId", entry.getKey());
                    item.put("knowledgeTitle", knowledge.map(Knowledge::getTitle).orElse("未知"));
                    item.put("errorCount", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("hotKnowledgeList", hotKnowledgeList);
        return result;
    }

    /**
     * 获取班级统计数据
     */
    public Map<String, Object> getClassStats(String className) {
        Map<String, Object> stats = new HashMap<>();
        
        // 通过classId字段查找班级学生
        List<User> students = userRepository.findByClassId(className);
        
        if (students.isEmpty()) {
            // 如果没有学生，返回空统计
            stats.put("studentCount", 0);
            stats.put("totalErrorCount", 0);
            stats.put("averageErrorPerStudent", 0.0);
            stats.put("subjectStats", new HashMap<>());
            stats.put("difficultyStats", new HashMap<>());
            stats.put("topErrors", new HashMap<>());
            return stats;
        }
        
        // 获取所有学生的错题
        List<ErrorBook> allClassErrors = new ArrayList<>();
        for (User student : students) {
            List<ErrorBook> studentErrors = errorBookRepository.findByUserOrderByCreatedAtDesc(student);
            allClassErrors.addAll(studentErrors);
        }
        
        // 统计数据
        stats.put("studentCount", students.size());
        stats.put("totalErrorCount", allClassErrors.size());
        stats.put("averageErrorPerStudent", 
            students.size() > 0 ? (double) allClassErrors.size() / students.size() : 0.0);
        
        // 学科分布统计
        Map<String, Long> subjectStats = allClassErrors.stream()
                .collect(Collectors.groupingBy(
                    eb -> eb.getQuestion().getSubject(),
                    Collectors.counting()
                ));
        stats.put("subjectStats", subjectStats);
        
        // 难度分布统计
        Map<String, Long> difficultyStats = allClassErrors.stream()
                .filter(eb -> eb.getQuestion().getDifficulty() != null)
                .collect(Collectors.groupingBy(
                    eb -> eb.getQuestion().getDifficulty(),
                    Collectors.counting()
                ));
        stats.put("difficultyStats", difficultyStats);
        
        // 高频错题 Top 10
        Map<String, Long> questionCount = allClassErrors.stream()
                .collect(Collectors.groupingBy(
                    eb -> eb.getQuestion().getContent().substring(0, 
                        Math.min(50, eb.getQuestion().getContent().length())),
                    Collectors.counting()
                ));
        
        Map<String, Long> topErrors = questionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        stats.put("topErrors", topErrors);
        
        return stats;
    }
}


