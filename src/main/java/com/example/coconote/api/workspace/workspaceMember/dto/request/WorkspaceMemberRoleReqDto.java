package com.example.coconote.api.workspace.workspaceMember.dto.request;

import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMemberRoleReqDto {
    private Long id;
    private WsRole wsRole;
}
