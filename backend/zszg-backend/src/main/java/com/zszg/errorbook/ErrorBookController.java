package com.zszg.errorbook;

import com.zszg.common.ApiResponse;
import com.zszg.service.FileStorageService;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errorbook")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ErrorBookController {
    private final ErrorBookService errorBookService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final QuestionParseService questionParseService;

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
     * 创建错题
     */
    @PostMapping
    public ApiResponse<ErrorBook> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateErrorDto dto) {
        try {
            // 详细日志，帮助调试
            System.out.println("📝 [创建错题] 用户: " + principal.getUsername());
            System.out.println("  - 学科: [" + dto.getSubject() + "]");
            System.out.println("  - 难度: [" + dto.getDifficulty() + "]");
            System.out.println("  - 题目内容长度: " + (dto.getContent() != null ? dto.getContent().length() : "null"));
            System.out.println("  - 答案长度: " + (dto.getAnswer() != null ? dto.getAnswer().length() : "null"));
            
            User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            
            ErrorBook errorBook = errorBookService.createErrorBookWithQuestion(
                    user, dto.getSubject(), dto.getDifficulty(), dto.getContent(),
                    dto.getAnswer(), dto.getAnalysis(), dto.getErrorReason(),
                    dto.getCorrection(), dto.getTags(), dto.getImages()
            );
            
            System.out.println("✅ [创建错题] 成功，ID: " + errorBook.getId());
            return ApiResponse.ok(errorBook);
        } catch (Exception e) {
            System.err.println("❌ [创建错题] 失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 上传错题图片
     */
    @PostMapping("/upload-image")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.storeFile(file, "errorbook");
            return ApiResponse.ok(url);
        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新错题
     */
    @PutMapping("/{id}")
    public ApiResponse<ErrorBook> update(
            @PathVariable Long id,
            @RequestBody UpdateErrorDto dto) {
        ErrorBook errorBook = errorBookService.updateErrorBook(
                id, dto.getErrorReason(), dto.getCorrection(), dto.getTags()
        );
        return ApiResponse.ok(errorBook);
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        errorBookService.deleteErrorBook(id, user);
        return ApiResponse.ok(null);
    }

    /**
     * 获取错题统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> statistics(
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ApiResponse.ok(errorBookService.getUserStatistics(user));
    }

    /**
     * 智能识别题目内容
     * 根据题目内容自动识别学科、难度、答案、解析等信息
     */
    @PostMapping("/parse-question")
    public ApiResponse<QuestionParseService.QuestionParseResult> parseQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ParseQuestionDto dto) {
        try {
            // 记录请求日志（用于调试）
            String username = principal != null ? principal.getUsername() : "匿名";
            System.out.println("🧠 [智能识别] 用户: " + username + ", 题目长度: " + dto.getContent().length());
            
            QuestionParseService.QuestionParseResult result = 
                questionParseService.parseQuestion(dto.getContent());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("智能识别失败: " + e.getMessage());
        }
    }

    @Data
    public static class CreateErrorDto {
        @NotBlank
        private String subject;
        private String difficulty;
        @NotBlank
        private String content;
        private String answer;
        private String analysis;
        private String errorReason;
        private String correction;
        private String tags;
        private String images; // JSON array of image URLs
    }

    @Data
    public static class UpdateErrorDto {
        private String errorReason;
        private String correction;
        private String tags;
    }

    @Data
    public static class ParseQuestionDto {
        @NotBlank(message = "题目内容不能为空")
        private String content;
    }
}
