package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .avatarUrl(entity.getAvatarUrl())
                .status(entity.getStatus())
                .phone(entity.getPhone())
                .roles(entity.getRoles() != null ?
                    entity.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

