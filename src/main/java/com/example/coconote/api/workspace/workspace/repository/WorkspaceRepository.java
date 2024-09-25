package com.example.coconote.api.workspace.workspace.repository;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByMemberAndIsDeleted(Member member, IsDeleted isDeleted);
}
