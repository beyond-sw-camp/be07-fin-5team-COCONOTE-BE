package com.example.coconote.api.workspaceMember.dto.request;

import com.example.coconote.api.workspaceMember.entity.WsRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberCreateReqDto {
    private Long workspaceId;
    private Long memberId;
    private String memberName;
    private String nickname;
    private String field;
    private String position;
    private String profileImage;
    private WsRole wsRole;

}
