package com.exe202.skillnest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteStudentRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Student ID is required")
    private Long studentId;
}
