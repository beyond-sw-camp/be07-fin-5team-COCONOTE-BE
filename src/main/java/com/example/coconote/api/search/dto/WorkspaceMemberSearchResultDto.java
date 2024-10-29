package com.example.coconote.api.search.dto;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberSearchResultDto {
    private Long workspaceMemberId;
    private String memberName;
    private String nickname;
    private String email;
    private String profileImage;


    //    fromDocument
    public static WorkspaceMemberSearchResultDto fromDocument(WorkspaceMemberDocument document) {
        return WorkspaceMemberSearchResultDto.builder()
                .workspaceMemberId(document.getWorkspaceMemberId())
                .memberName(document.getMemberName())
                .nickname(document.getNickname())
                .email(document.getEmail())
                .profileImage(document.getProfileImage())
                .build();
    }
}
