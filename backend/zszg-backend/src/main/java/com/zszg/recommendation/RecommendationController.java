package com.zszg.recommendation;

import com.zszg.common.ApiResponse;
import com.zszg.question.Question;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    /**
     * 获取个性化推荐题目
     */
    @GetMapping
    public ApiResponse<List<Question>> getRecommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Question> recommendations = recommendationService.getRecommendedQuestions(user, limit);
        return ApiResponse.ok(recommendations);
    }

    /**
     * 根据知识点推荐题目
     */
    @GetMapping("/by-knowledge/{knowledgeId}")
    public ApiResponse<List<Question>> getRecommendationsByKnowledge(
            @PathVariable Long knowledgeId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Question> recommendations = recommendationService.getQuestionsByKnowledge(knowledgeId, limit);
        return ApiResponse.ok(recommendations);
    }

    /**
     * 根据学科推荐题目
     */
    @GetMapping("/by-subject")
    public ApiResponse<List<Question>> getRecommendationsBySubject(
            @AuthenticationPrincipal User user,
            @RequestParam String subject,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Question> recommendations = recommendationService.getQuestionsBySubject(user, subject, limit);
        return ApiResponse.ok(recommendations);
    }
}


