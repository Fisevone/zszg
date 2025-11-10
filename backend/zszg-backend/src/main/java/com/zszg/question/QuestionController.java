package com.zszg.question;

import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    /**
     * 获取所有题目
     */
    @GetMapping
    public ApiResponse<List<Question>> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String difficulty) {
        // 简化查询，实际应该用Specification
        List<Question> questions = questionRepository.findAll();
        return ApiResponse.ok(questions);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Question> get(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        return ApiResponse.ok(question);
    }

    /**
     * 创建题目（管理员/教师）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Question> create(
            @Valid @RequestBody CreateQuestionDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        User creator = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        
        Question question = Question.builder()
                .subject(dto.getSubject())
                .difficulty(dto.getDifficulty())
                .content(dto.getContent())
                .answer(dto.getAnswer())
                .analysis(dto.getAnalysis())
                .source(dto.getSource())
                .createdBy(creator)
                .build();
        
        questionRepository.save(question);
        return ApiResponse.ok(question);
    }

    /**
     * 更新题目（管理员/教师）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Question> update(
            @PathVariable Long id,
            @RequestBody UpdateQuestionDto dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        if (dto.getSubject() != null) question.setSubject(dto.getSubject());
        if (dto.getDifficulty() != null) question.setDifficulty(dto.getDifficulty());
        if (dto.getContent() != null) question.setContent(dto.getContent());
        if (dto.getAnswer() != null) question.setAnswer(dto.getAnswer());
        if (dto.getAnalysis() != null) question.setAnalysis(dto.getAnalysis());
        if (dto.getSource() != null) question.setSource(dto.getSource());

        questionRepository.save(question);
        return ApiResponse.ok(question);
    }

    /**
     * 删除题目（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateQuestionDto {
        @NotBlank
        private String subject;
        private String difficulty;
        @NotBlank
        private String content;
        private String answer;
        private String analysis;
        private String source;
    }

    @Data
    public static class UpdateQuestionDto {
        private String subject;
        private String difficulty;
        private String content;
        private String answer;
        private String analysis;
        private String source;
    }
}


