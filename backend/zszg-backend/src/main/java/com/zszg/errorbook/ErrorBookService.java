package com.zszg.errorbook;

import com.zszg.question.Question;
import com.zszg.question.QuestionRepository;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ErrorBookService {
    private final ErrorBookRepository errorBookRepository;
    private final QuestionRepository questionRepository;

    /**
     * 获取用户的错题列表
     */
    public List<ErrorBook> getUserErrorBooks(User user) {
        return errorBookRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 按学科筛选错题
     */
    public List<ErrorBook> getUserErrorBooksBySubject(User user, String subject) {
        return errorBookRepository.findByUserAndSubject(user, subject);
    }

    /**
     * 创建错题
     */
    @Transactional
    public ErrorBook createErrorBook(User user, Question question, String errorReason, 
                                      String correction, String tags, String images) {
        // 处理images字段：确保是有效的JSON格式
        String validImages = images;
        if (images == null || images.trim().isEmpty()) {
            validImages = "[]";
        }
        
        ErrorBook errorBook = ErrorBook.builder()
                .user(user)
                .question(question)
                .errorReason(errorReason)
                .correction(correction)
                .tags(tags)
                .images(validImages)
                .status("PRIVATE")
                .build();
        return errorBookRepository.save(errorBook);
    }

    /**
     * 创建错题（同时创建题目）
     */
    @Transactional
    public ErrorBook createErrorBookWithQuestion(User user, String subject, String difficulty,
                                                  String content, String answer, String analysis,
                                                  String errorReason, String correction, 
                                                  String tags, String images) {
        Question question = Question.builder()
                .subject(subject)
                .difficulty(difficulty)
                .content(content)
                .answer(answer)
                .analysis(analysis)
                .source("user")
                .createdBy(user)
                .build();
        questionRepository.save(question);

        // 处理images字段：空字符串或null转换为空JSON数组
        String validImages = images;
        if (images == null || images.trim().isEmpty()) {
            validImages = "[]";
        } else if (!images.trim().startsWith("[") && !images.trim().startsWith("{")) {
            // 如果不是JSON格式，包装成数组
            validImages = "[]";
        }

        return createErrorBook(user, question, errorReason, correction, tags, validImages);
    }

    /**
     * 更新错题
     */
    @Transactional
    public ErrorBook updateErrorBook(Long id, String errorReason, String correction, String tags) {
        ErrorBook errorBook = errorBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        if (errorReason != null) errorBook.setErrorReason(errorReason);
        if (correction != null) errorBook.setCorrection(correction);
        if (tags != null) errorBook.setTags(tags);
        
        return errorBookRepository.save(errorBook);
    }

    /**
     * 删除错题
     */
    @Transactional
    public void deleteErrorBook(Long id, User user) {
        ErrorBook errorBook = errorBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        if (!errorBook.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此错题");
        }
        
        errorBookRepository.delete(errorBook);
    }

    /**
     * 获取错题统计信息
     */
    public Map<String, Object> getUserStatistics(User user) {
        Map<String, Object> stats = new HashMap<>();
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        long totalCount = errorBooks.size();
        long sharedCount = errorBooks.stream()
                .filter(e -> "SHARED".equals(e.getStatus()))
                .count();
        
        // 按学科统计
        Map<String, Long> subjectStats = new HashMap<>();
        for (ErrorBook eb : errorBooks) {
            String subject = eb.getQuestion().getSubject();
            subjectStats.put(subject, subjectStats.getOrDefault(subject, 0L) + 1);
        }
        
        stats.put("totalCount", totalCount);
        stats.put("sharedCount", sharedCount);
        stats.put("subjectStats", subjectStats);
        
        return stats;
    }
}

