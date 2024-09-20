package com.example.coconote.api.section.dto.request;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionCreateReqDto {
    private Long workspaceId;
    private String sectionName;

    public Section toEntity(Workspace workspace) {
        return Section.builder()
                .workspace(workspace)
                .sectionName(this.sectionName)
                .build();
    }


}
