package com.recruitcrm.rts.dto;

import com.recruitcrm.rts.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "Role is required (RECRUITER or CANDIDATE)")
        Role role,

        // required only when role is RECRUITER (checked in UserService)
        String companyName
) {
}
