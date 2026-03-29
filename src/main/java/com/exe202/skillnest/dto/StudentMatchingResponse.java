package com.exe202.skillnest.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMatchingResponse {
    private List<StudentMatchDTO> students;
    private int total;
    private int page;
    private int limit;
}
