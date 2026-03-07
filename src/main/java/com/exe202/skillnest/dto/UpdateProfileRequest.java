package com.exe202.skillnest.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 200, message = "University must not exceed 200 characters")
    private String university;

    @Size(max = 200, message = "Major must not exceed 200 characters")
    private String major;

    @NotBlank(message = "Year is required")
    @Size(max = 50, message = "Year must not exceed 50 characters")
    private String year;

    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA must not exceed 4.0")
    private Double gpa;

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatar;

    @Size(max = 50, message = "Skills list is too large")
    private List<String> skills;

    @Size(max = 50, message = "Interests list is too large")
    private List<String> interests;

    @Size(max = 20, message = "Preferred locations list is too large")
    private List<String> preferredLocations;

    @Size(max = 20, message = "Preferred job types list is too large")
    private List<String> preferredJobTypes;
}

