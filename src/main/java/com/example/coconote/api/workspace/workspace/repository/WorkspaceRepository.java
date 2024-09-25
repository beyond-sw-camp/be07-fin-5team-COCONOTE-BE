package com.example.coconote.api.workspace.workspace.repository;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByWorkspaceMemberAndIsDeleted(WorkspaceMember workspaceMember, IsDeleted isDeleted);
}
