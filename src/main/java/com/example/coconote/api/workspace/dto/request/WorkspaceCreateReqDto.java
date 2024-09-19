package com.example.coconote.api.workspace.dto.request;

import com.example.coconote.api.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceCreateReqDto {
    private String name;
    private String info;

    public Workspace toEntity() {
        return Workspace.builder()
                .name(this.name)
                .info(this.info)
                .build();
    }
}
