package com.zszg.admin;

import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户
     */
    @GetMapping
    public ApiResponse<List<User>> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        if (role != null && !role.isEmpty()) {
            return ApiResponse.ok(userRepository.findByRole(role));
        }
        if (status != null && !status.isEmpty()) {
            return ApiResponse.ok(userRepository.findByStatus(status));
        }
        return ApiResponse.ok(userRepository.findAll());
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody CreateUserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .role(dto.getRole())
                .status("ACTIVE")
                .classId(dto.getClassId())
                .grade(dto.getGrade())
                .build();
        
        userRepository.save(user);
        return ApiResponse.ok(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (dto.getClassId() != null) user.setClassId(dto.getClassId());
        if (dto.getGrade() != null) user.setGrade(dto.getGrade());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return ApiResponse.ok(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ApiResponse.ok(null);
    }

    /**
     * 冻结/激活用户
     */
    @PostMapping("/{id}/toggle-status")
    public ApiResponse<User> toggleStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStatus("ACTIVE".equals(user.getStatus()) ? "FROZEN" : "ACTIVE");
        userRepository.save(user);
        return ApiResponse.ok(user);
    }

    @Data
    public static class CreateUserDto {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private String realName;
        private String phone;
        private String email;
        @NotBlank
        private String role; // ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN
        private String classId;
        private String grade;
    }

    @Data
    public static class UpdateUserDto {
        private String realName;
        private String phone;
        private String email;
        private String role;
        private String status;
        private String classId;
        private String grade;
        private String password;
    }
}
