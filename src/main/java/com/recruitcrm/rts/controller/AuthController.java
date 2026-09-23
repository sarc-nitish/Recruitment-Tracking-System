package com.recruitcrm.rts.controller;

import com.recruitcrm.rts.dto.RegisterRequest;
import com.recruitcrm.rts.dto.UserResponse;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** POST /api/auth/register  (public) */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    /** GET /api/auth/me  - who am I? Handy to check that your login works in Postman. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return UserResponse.from(currentUser);
    }
}
