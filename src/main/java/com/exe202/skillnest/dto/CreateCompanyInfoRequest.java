package com.exe202.skillnest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyInfoRequest {
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 100, message = "Size must not exceed 100 characters")
    private String size; // e.g., "100-500 nhân viên"

    @Size(max = 200, message = "Industry must not exceed 200 characters")
    private String industry; // e.g., "Công nghệ thông tin"
}

