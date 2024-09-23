package com.example.coconote.api.workspaceMember.repository;

import com.example.coconote.api.workspaceMember.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
}
