package com.zszg.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String realName;
    private String email;
    private String role; // ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN，默认为学生
}



