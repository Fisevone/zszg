package com.zszg.stats;

import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    /**
     * 获取学生个人统计数据
     */
    @GetMapping("/user")
    public ApiResponse<Map<String, Object>> getUserStats(@AuthenticationPrincipal User user) {
        Map<String, Object> stats = statsService.getUserStats(user);
        return ApiResponse.ok(stats);
    }

    /**
     * 获取教师数据面板统计（教师/管理员）
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getTeacherStats() {
        Map<String, Object> stats = statsService.getTeacherStats();
        return ApiResponse.ok(stats);
    }

    /**
     * 获取学科错题统计
     */
    @GetMapping("/subject")
    public ApiResponse<Map<String, Long>> getSubjectStats(@AuthenticationPrincipal User user) {
        Map<String, Long> stats = statsService.getSubjectStats(user);
        return ApiResponse.ok(stats);
    }

    /**
     * 获取难度分布统计
     */
    @GetMapping("/difficulty")
    public ApiResponse<Map<String, Long>> getDifficultyStats(@AuthenticationPrincipal User user) {
        Map<String, Long> stats = statsService.getDifficultyStats(user);
        return ApiResponse.ok(stats);
    }

    /**
     * 获取知识点热度统计（教师/管理员）
     */
    @GetMapping("/knowledge-hotness")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getKnowledgeHotness() {
        Map<String, Object> stats = statsService.getKnowledgeHotness();
        return ApiResponse.ok(stats);
    }

    /**
     * 获取班级统计数据（教师/管理员）
     */
    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getClassStats(@PathVariable String className) {
        Map<String, Object> stats = statsService.getClassStats(className);
        return ApiResponse.ok(stats);
    }
}


