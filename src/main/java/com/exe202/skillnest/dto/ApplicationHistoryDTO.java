package com.exe202.skillnest.dto;

import com.exe202.skillnest.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryDTO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private ApplicationStatus status;
    private LocalDateTime appliedDate;
}

