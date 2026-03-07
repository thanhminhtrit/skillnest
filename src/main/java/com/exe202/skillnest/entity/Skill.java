package com.exe202.skillnest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skills", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long skillId;

    @Column(nullable = false, unique = true, length = 120)
    private String name;
}
