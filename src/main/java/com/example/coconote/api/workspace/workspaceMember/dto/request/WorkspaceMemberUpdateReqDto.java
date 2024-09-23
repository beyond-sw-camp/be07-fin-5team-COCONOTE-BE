package com.example.coconote.api.workspace.workspaceMember.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberUpdateReqDto {
    private String memberName;
    private String nickname;
    private String field;
    private String position;
    private String profileImage;
}
