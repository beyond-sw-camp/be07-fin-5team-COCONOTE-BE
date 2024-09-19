package com.example.coconote.api.workspace.entity;

import com.example.coconote.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String wsInfo;

    private String logoImagePath;
}
