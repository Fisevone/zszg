package com.zszg.classroom;

import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassRoomService {
    private final ClassRoomRepository classRoomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final TeacherPushRepository teacherPushRepository;
    private final TeacherFeedbackRepository teacherFeedbackRepository;
    private final UserRepository userRepository;
    private final ErrorBookRepository errorBookRepository;

    // 创建班级
    @Transactional
    public ClassRoom createClass(Long teacherId, String name, String gradeLevel, String description) {
        String inviteCode = generateInviteCode();
        ClassRoom classRoom = ClassRoom.builder()
                .name(name)
                .gradeLevel(gradeLevel)
                .teacherId(teacherId)
                .inviteCode(inviteCode)
                .description(description)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return classRoomRepository.save(classRoom);
    }

    // 生成邀请码
    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        // 检查是否重复
        if (classRoomRepository.findByInviteCode(code.toString()).isPresent()) {
            return generateInviteCode();
        }
        return code.toString();
    }

    // 学生加入班级
    @Transactional
    public ClassMember joinClass(Long studentId, String inviteCode) {
        ClassRoom classRoom = classRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("邀请码无效"));
        
        // 检查是否已加入
        Optional<ClassMember> existing = classMemberRepository.findByClassIdAndStudentId(
                classRoom.getId(), studentId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("您已加入该班级");
        }
        
        ClassMember member = ClassMember.builder()
                .classId(classRoom.getId())
                .studentId(studentId)
                .joinedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        return classMemberRepository.save(member);
    }

    // 获取教师的班级列表
    public List<Map<String, Object>> getTeacherClasses(Long teacherId) {
        List<ClassRoom> classes = classRoomRepository.findByTeacherId(teacherId);
        return classes.stream().map(cls -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cls.getId());
            map.put("name", cls.getName());
            map.put("gradeLevel", cls.getGradeLevel());
            map.put("inviteCode", cls.getInviteCode());
            map.put("description", cls.getDescription());
            map.put("studentCount", classMemberRepository.countByClassId(cls.getId()));
            map.put("createdAt", cls.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    // 获取班级学生列表
    public List<Map<String, Object>> getClassStudents(Long classId) {
        List<ClassMember> members = classMemberRepository.findByClassId(classId);
        return members.stream().map(member -> {
            User student = userRepository.findById(member.getStudentId()).orElse(null);
            if (student == null) return null;
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", student.getId());
            map.put("username", student.getUsername());
            map.put("realName", student.getRealName());
            map.put("joinedAt", member.getJoinedAt());
            
            // 统计错题数
            long errorCount = errorBookRepository.countByUser(student);
            map.put("errorCount", errorCount);
            
            return map;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // 教师推送资源
    @Transactional
    public TeacherPush pushResource(Long teacherId, Long classId, String title, String content, 
                                    String resourceType, Long resourceId) {
        TeacherPush push = TeacherPush.builder()
                .teacherId(teacherId)
                .classId(classId)
                .title(title)
                .content(content)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .createdAt(LocalDateTime.now())
                .build();
        return teacherPushRepository.save(push);
    }

    // 学生获取推送列表
    public List<Map<String, Object>> getStudentPushes(Long studentId) {
        // 获取学生加入的班级
        List<ClassMember> memberships = classMemberRepository.findByStudentId(studentId);
        List<Map<String, Object>> allPushes = new ArrayList<>();
        
        for (ClassMember membership : memberships) {
            List<TeacherPush> pushes = teacherPushRepository.findByClassIdOrderByCreatedAtDesc(
                    membership.getClassId());
            
            for (TeacherPush push : pushes) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", push.getId());
                map.put("title", push.getTitle());
                map.put("content", push.getContent());
                map.put("resourceType", push.getResourceType());
                map.put("resourceId", push.getResourceId());
                map.put("createdAt", push.getCreatedAt());
                
                // 获取教师信息
                User teacher = userRepository.findById(push.getTeacherId()).orElse(null);
                if (teacher != null) {
                    map.put("teacherName", teacher.getRealName());
                }
                
                // 获取班级信息
                ClassRoom classRoom = classRoomRepository.findById(push.getClassId()).orElse(null);
                if (classRoom != null) {
                    map.put("className", classRoom.getName());
                }
                
                allPushes.add(map);
            }
        }
        
        // 按时间倒序排序
        allPushes.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("createdAt");
            LocalDateTime timeB = (LocalDateTime) b.get("createdAt");
            return timeB.compareTo(timeA);
        });
        
        return allPushes;
    }

    // 教师添加反馈
    @Transactional
    public TeacherFeedback addFeedback(Long teacherId, Long studentId, Long errorBookId, 
                                       String feedback, Integer rating) {
        TeacherFeedback fb = TeacherFeedback.builder()
                .teacherId(teacherId)
                .studentId(studentId)
                .errorBookId(errorBookId)
                .feedback(feedback)
                .rating(rating)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return teacherFeedbackRepository.save(fb);
    }

    // 获取错题的反馈
    public List<Map<String, Object>> getErrorBookFeedbacks(Long errorBookId) {
        List<TeacherFeedback> feedbacks = teacherFeedbackRepository.findByErrorBookId(errorBookId);
        return feedbacks.stream().map(fb -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", fb.getId());
            map.put("feedback", fb.getFeedback());
            map.put("rating", fb.getRating());
            map.put("createdAt", fb.getCreatedAt());
            
            User teacher = userRepository.findById(fb.getTeacherId()).orElse(null);
            if (teacher != null) {
                map.put("teacherName", teacher.getRealName());
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    // 获取学生的班级列表（修复：添加班级人数和推送数量统计）
    public List<Map<String, Object>> getStudentClasses(Long studentId) {
        List<ClassMember> memberships = classMemberRepository.findByStudentId(studentId);
        return memberships.stream().map(member -> {
            ClassRoom classRoom = classRoomRepository.findById(member.getClassId()).orElse(null);
            if (classRoom == null) return null;
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", classRoom.getId());
            map.put("classId", classRoom.getId());  // 前端需要classId
            map.put("className", classRoom.getName());  // 前端显示className
            map.put("gradeLevel", classRoom.getGradeLevel());
            map.put("description", classRoom.getDescription());  // 添加描述
            map.put("joinedAt", member.getJoinedAt());
            
            // 统计班级人数（包括学生自己！）
            long studentCount = classMemberRepository.countByClassId(classRoom.getId());
            map.put("studentCount", studentCount);
            
            // 统计教师推送数量
            long pushCount = teacherPushRepository.countByClassId(classRoom.getId());
            map.put("pushCount", pushCount);
            
            // 获取教师信息
            User teacher = userRepository.findById(classRoom.getTeacherId()).orElse(null);
            if (teacher != null) {
                map.put("teacherName", teacher.getRealName());
            }
            
            return map;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // 获取班级的推送列表
    public List<TeacherPush> getClassPushes(Long classId) {
        return teacherPushRepository.findByClassIdOrderByCreatedAtDesc(classId);
    }

    // 退出班级
    @Transactional
    public void leaveClass(Long studentId, Long classId) {
        classMemberRepository.findByClassIdAndStudentId(classId, studentId)
                .ifPresent(classMemberRepository::delete);
    }

    // 删除班级（教师）
    @Transactional
    public void deleteClass(Long teacherId, Long classId) {
        // 验证班级是否属于该教师
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        
        if (!classRoom.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("无权删除该班级");
        }
        
        // 删除相关数据
        // 1. 删除教师推送
        teacherPushRepository.deleteByClassId(classId);
        
        // 2. 删除班级成员
        classMemberRepository.deleteByClassId(classId);
        
        // 3. 删除班级
        classRoomRepository.delete(classRoom);
        
        // 注意：不删除教师反馈，保留作为历史记录
    }
}


