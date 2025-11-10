package com.zszg.classroom;

import com.zszg.common.ApiResponse;
import com.zszg.security.JwtUtil;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassRoomController {
    private final ClassRoomService classRoomService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 创建班级（教师）
    @PostMapping("/create")
    public ApiResponse<ClassRoom> createClass(@RequestBody CreateClassDto dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ClassRoom classRoom = classRoomService.createClass(userId, dto.getName(), 
                dto.getGradeLevel(), dto.getDescription());
        return ApiResponse.ok(classRoom);
    }

    // 加入班级（学生）
    @PostMapping("/join")
    public ApiResponse<ClassMember> joinClass(@RequestBody JoinClassDto dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ClassMember member = classRoomService.joinClass(userId, dto.getInviteCode());
        return ApiResponse.ok(member);
    }

    // 获取教师的班级列表
    @GetMapping("/teacher/classes")
    public ApiResponse<List<Map<String, Object>>> getTeacherClasses(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Map<String, Object>> classes = classRoomService.getTeacherClasses(userId);
        return ApiResponse.ok(classes);
    }

    // 获取班级学生列表
    @GetMapping("/class/{classId}/students")
    public ApiResponse<List<Map<String, Object>>> getClassStudents(@PathVariable Long classId) {
        List<Map<String, Object>> students = classRoomService.getClassStudents(classId);
        return ApiResponse.ok(students);
    }

    // 教师推送资源
    @PostMapping("/push")
    public ApiResponse<TeacherPush> pushResource(@RequestBody PushResourceDto dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TeacherPush push = classRoomService.pushResource(userId, dto.getClassId(), dto.getTitle(),
                dto.getContent(), dto.getResourceType(), dto.getResourceId());
        return ApiResponse.ok(push);
    }

    // 学生获取推送列表
    @GetMapping("/student/pushes")
    public ApiResponse<List<Map<String, Object>>> getStudentPushes(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Map<String, Object>> pushes = classRoomService.getStudentPushes(userId);
        return ApiResponse.ok(pushes);
    }

    // 教师添加反馈
    @PostMapping("/feedback")
    public ApiResponse<TeacherFeedback> addFeedback(@RequestBody AddFeedbackDto dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TeacherFeedback feedback = classRoomService.addFeedback(userId, dto.getStudentId(),
                dto.getErrorBookId(), dto.getFeedback(), dto.getRating());
        return ApiResponse.ok(feedback);
    }

    // 获取错题的反馈
    @GetMapping("/feedback/errorbook/{errorBookId}")
    public ApiResponse<List<Map<String, Object>>> getErrorBookFeedbacks(@PathVariable Long errorBookId) {
        List<Map<String, Object>> feedbacks = classRoomService.getErrorBookFeedbacks(errorBookId);
        return ApiResponse.ok(feedbacks);
    }

    // 获取学生的班级列表
    @GetMapping("/student/classes")
    public ApiResponse<List<Map<String, Object>>> getStudentClasses(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Map<String, Object>> classes = classRoomService.getStudentClasses(userId);
        return ApiResponse.ok(classes);
    }

    // 获取班级的推送列表
    @GetMapping("/class/{classId}/pushes")
    public ApiResponse<List<Map<String, Object>>> getClassPushes(@PathVariable Long classId) {
        List<TeacherPush> pushes = classRoomService.getClassPushes(classId);
        List<Map<String, Object>> result = pushes.stream().map(push -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", push.getId());
            map.put("title", push.getTitle());
            map.put("content", push.getContent());
            map.put("resourceType", push.getResourceType());
            map.put("createdAt", push.getCreatedAt());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ApiResponse.ok(result);
    }

    // 退出班级
    @DeleteMapping("/class/{classId}/leave")
    public ApiResponse<Void> leaveClass(@PathVariable Long classId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        classRoomService.leaveClass(userId, classId);
        return ApiResponse.ok(null);
    }

    // 删除班级（教师）
    @DeleteMapping("/class/{classId}")
    public ApiResponse<Void> deleteClass(@PathVariable Long classId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        classRoomService.deleteClass(userId, classId);
        return ApiResponse.ok(null);
    }

    // 从请求中获取用户ID
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            return user.getId();
        }
        throw new IllegalArgumentException("未找到认证信息");
    }

    // DTO classes
    @Data
    public static class CreateClassDto {
        private String name;
        private String gradeLevel;
        private String description;
    }

    @Data
    public static class JoinClassDto {
        private String inviteCode;
    }

    @Data
    public static class PushResourceDto {
        private Long classId;
        private String title;
        private String content;
        private String resourceType; // KNOWLEDGE, QUESTION, NOTICE
        private Long resourceId;
    }

    @Data
    public static class AddFeedbackDto {
        private Long studentId;
        private Long errorBookId;
        private String feedback;
        private Integer rating;
    }
}


