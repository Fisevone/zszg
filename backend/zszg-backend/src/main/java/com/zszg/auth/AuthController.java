package com.zszg.auth;

import com.zszg.auth.dto.AuthResponse;
import com.zszg.auth.dto.LoginRequest;
import com.zszg.auth.dto.RegisterRequest;
import com.zszg.common.ApiResponse;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<User> me(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        user.setPasswordHash(null);
        return ApiResponse.ok(user);
    }
    
    @PutMapping("/update")
    public ApiResponse<User> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody java.util.Map<String, String> updates) {
        User currentUser = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        User updatedUser = authService.updateProfile(
                currentUser,
                updates.get("realName"),
                updates.get("email"),
                updates.get("password")
        );
        updatedUser.setPasswordHash(null);
        return ApiResponse.ok(updatedUser);
    }
}



