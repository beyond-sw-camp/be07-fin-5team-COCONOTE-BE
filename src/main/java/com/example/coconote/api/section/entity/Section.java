package com.example.coconote.api.section.entity;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "section", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    public SectionListResDto fromEntity() {
        return SectionListResDto.builder()
                .name(this.name).build();
    }

    public void updateEntity(SectionUpdateReqDto dto) {
        this.name = dto.getName();
    }
}