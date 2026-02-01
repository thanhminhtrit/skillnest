package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.AuthResponse;
import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.payloads.request.LoginRequest;
import com.exe202.skillnest.payloads.request.RegisterRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<BaseResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<BaseResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(new BaseResponse(200, "Login successful", authResponse));
    }

    //check lai Request o header
    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    public ResponseEntity<BaseResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserDTO userDTO = authService.getCurrentUser(email);
        return ResponseEntity.ok(new BaseResponse(200, "User retrieved successfully", userDTO));
    }
}

