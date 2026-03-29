package com.exe202.skillnest.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMatchDTO {
    private Long studentId;
    private String name;
    private String avatar;

    // Academic info
    private String university;
    private String major;
    private String year;
    private Double gpa;

    // Skills
    private List<String> skills;
    private List<String> matchingSkills;

    // Matching
    private Integer matchScore;
    private String matchReason;
}
