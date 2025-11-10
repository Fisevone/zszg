package com.zszg.errorbook;

import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题本API（兼容前端调用 /api/error-books 路径）
 */
@RestController
@RequestMapping("/api/error-books")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ErrorBooksController {
    private final ErrorBookService errorBookService;
    private final UserRepository userRepository;

    /**
     * 获取用户错题列表
     */
    @GetMapping
    public ApiResponse<List<ErrorBook>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String subject) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        
        if (subject != null && !subject.isEmpty()) {
            return ApiResponse.ok(errorBookService.getUserErrorBooksBySubject(user, subject));
        }
        return ApiResponse.ok(errorBookService.getUserErrorBooks(user));
    }

    /**
     * 创建错题（从推荐或共享池复制）
     */
    @PostMapping
    public ApiResponse<ErrorBook> create(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody CreateFromRecommendationDto dto) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        
        ErrorBook errorBook = errorBookService.createErrorBookWithQuestion(
                user, 
                dto.getSubject() != null ? dto.getSubject() : "未知", 
                dto.getDifficulty() != null ? dto.getDifficulty() : "中等", 
                dto.getContent(),
                dto.getAnswer() != null ? dto.getAnswer() : "",
                dto.getAnalysis() != null ? dto.getAnalysis() : "",
                dto.getErrorReason() != null ? dto.getErrorReason() : "",
                dto.getCorrection() != null ? dto.getCorrection() : "",
                dto.getTags() != null ? dto.getTags() : "",
                dto.getImages() != null ? dto.getImages() : ""
        );
        
        return ApiResponse.ok(errorBook);
    }

    @Data
    public static class CreateFromRecommendationDto {
        private Long questionId; // 如果从题库复制
        private String subject;
        private String difficulty;
        private String content;
        private String answer;
        private String analysis;
        private String errorReason;
        private String correction;
        private String tags;
        private String images;
    }
}







