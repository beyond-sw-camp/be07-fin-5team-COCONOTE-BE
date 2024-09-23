package com.example.coconote.api.workspace.workspace.entity;

import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private Long workspaceId;

    private String name;

    private String wsInfo;

    private String logo;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    public WorkspaceListResDto fromEntity() {
        return WorkspaceListResDto.builder()
                .workspaceId(this.workspaceId)
                .name(this.name)
                .wsInfo(this.wsInfo)
                .build();
    }

    public void updateEntity(WorkspaceUpdateReqDto dto) {
        this.name = dto.getName();
        this.wsInfo = dto.getWsInfo();
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
        if(this.sections != null) {
            for (Section s : this.sections) {
                s.deleteEntity();
            }
        }
    }
}
