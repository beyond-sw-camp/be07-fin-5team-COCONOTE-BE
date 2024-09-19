package com.example.coconote.api.workspace.entity;

import com.example.coconote.api.section.entity.Section;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

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

    private String info;

    private String logoImagePath;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

}
