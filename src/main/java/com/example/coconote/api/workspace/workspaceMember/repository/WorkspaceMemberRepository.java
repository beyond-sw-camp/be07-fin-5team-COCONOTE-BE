package com.example.coconote.api.workspace.workspaceMember.repository;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByWorkspaceAndIsDeleted(Workspace workspace, IsDeleted isDeleted);
    Optional<WorkspaceMember> findByMemberAndWorkspace(Member member, Workspace workspace);
}
