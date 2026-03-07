package com.exe202.skillnest.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String avatar;
    private String phone;
    private String address;

    private String university;
    private String major;
    private String year;
    private Double gpa;
    private String bio;

    private List<String> skills;
    private List<String> interests;
    private List<String> preferredLocations;
    private List<String> preferredJobTypes;
}

