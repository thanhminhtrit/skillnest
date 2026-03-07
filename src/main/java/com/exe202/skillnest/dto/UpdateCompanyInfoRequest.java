package com.exe202.skillnest.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyInfoRequest {
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 100, message = "Size must not exceed 100 characters")
    private String size;

    @Size(max = 200, message = "Industry must not exceed 200 characters")
    private String industry;
}

