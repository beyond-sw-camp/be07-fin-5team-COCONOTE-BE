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
    private String wsInfo;

    public Workspace toEntity(String imgUrl) {
        return Workspace.builder()
                .name(this.name)
                .wsInfo(this.wsInfo)
                .logo(imgUrl)
                .build();
    }
}
