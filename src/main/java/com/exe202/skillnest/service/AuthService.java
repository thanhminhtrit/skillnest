package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.AuthResponse;
import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.payloads.request.LoginRequest;
import com.exe202.skillnest.payloads.request.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDTO getCurrentUser(String email);
}
