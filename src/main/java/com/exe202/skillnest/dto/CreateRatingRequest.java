package com.exe202.skillnest.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {
    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
}
