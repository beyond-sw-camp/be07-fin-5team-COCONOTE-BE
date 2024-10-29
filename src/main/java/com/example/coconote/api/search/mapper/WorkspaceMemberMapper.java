package com.example.coconote.api.search.mapper;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMemberMapper {

    // WorkspaceMember -> WorkspaceMemberDocument 변환
    public WorkspaceMemberDocument toDocument(WorkspaceMember workspaceMember) {
        return WorkspaceMemberDocument.builder()
                .id(String.valueOf(workspaceMember.getWorkspaceMemberId()))  // 문서 ID는 workspaceMemberId
                .workspaceMemberId(workspaceMember.getWorkspaceMemberId())
                .workspaceId(workspaceMember.getWorkspace().getWorkspaceId())
                .memberId(workspaceMember.getMember().getId())
                .memberName(workspaceMember.getMemberName())
                .nickname(workspaceMember.getNickname())
                .email(workspaceMember.getMember().getEmail())  // Member 엔티티의 email 가져오기
                .field(workspaceMember.getField())
                .position(workspaceMember.getPosition())
                .profileImage(workspaceMember.getProfileImage())
                .isDeleted(workspaceMember.getIsDeleted())
                .wsRole(workspaceMember.getWsRole().name())
                .build();
    }
}
