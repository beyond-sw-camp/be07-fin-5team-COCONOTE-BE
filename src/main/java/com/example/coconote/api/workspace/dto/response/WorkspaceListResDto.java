package com.example.coconote.api.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceListResDto {
    private Long workspaceId;
    private String name;
    private String wsInfo;
}
