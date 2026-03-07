package com.exe202.skillnest.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataResponseDTO {
    private List<String> locations;
    private List<String> jobTypes;
}

