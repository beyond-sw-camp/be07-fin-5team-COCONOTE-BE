package com.example.coconote.api.section.entity;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sectionId;

    private String sectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "section", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    public SectionListResDto fromEntity() {
        return SectionListResDto.builder()
                .sectionName(this.sectionName).build();
    }

    public void updateEntity(SectionUpdateReqDto dto) {
        this.sectionName = dto.getSectionName();
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
        if(this.channels != null) {
            for (Channel c : this.channels) {
                c.deleteEntity();
            }
        }
    }
}