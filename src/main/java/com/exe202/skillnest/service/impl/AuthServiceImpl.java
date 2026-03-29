package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.AuthResponse;
import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.entity.Role;
import com.exe202.skillnest.entity.SubscriptionPlan;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserSubscription;
import com.exe202.skillnest.enums.SubscriptionStatus;
import com.exe202.skillnest.enums.UserStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ConflictException;
import com.exe202.skillnest.payloads.request.LoginRequest;
import com.exe202.skillnest.payloads.request.RegisterRequest;
import com.exe202.skillnest.repository.RoleRepository;
import com.exe202.skillnest.repository.SubscriptionPlanRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.repository.UserSubscriptionRepository;
import com.exe202.skillnest.service.AuthService;
import com.exe202.skillnest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        // Validate role
        if (!request.getRole().equalsIgnoreCase("STUDENT") && !request.getRole().equalsIgnoreCase("CLIENT")) {
            throw new BadRequestException("Invalid role. Must be STUDENT or CLIENT");
        }

        // Find role (must exist — seeded at startup)
        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.getRole() + ". Available roles: STUDENT, CLIENT"));

        // Create user
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .tokenVersion(0)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        // Auto-assign FREE subscription plan
        SubscriptionPlan freePlan = subscriptionPlanRepository.findByName("FREE").orElse(null);
        if (freePlan != null) {
            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .plan(freePlan)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(freePlan.getDurationDays()))
                    .status(SubscriptionStatus.ACTIVE)
                    .postsUsed(0)
                    .aiMatchingUsed(0)
                    .autoRenew(true)
                    .build();
            userSubscriptionRepository.save(subscription);
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

        // Convert to DTO
        UserDTO userDTO = convertToUserDTO(user);

        return new AuthResponse(token, userDTO);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        // Check if user is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

        // Convert to DTO
        UserDTO userDTO = convertToUserDTO(user);

        return new AuthResponse(token, userDTO);
    }

    @Override
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return convertToUserDTO(user);
    }

    private UserDTO convertToUserDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .phone(user.getPhone())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
