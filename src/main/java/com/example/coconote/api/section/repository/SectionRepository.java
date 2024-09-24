package com.example.coconote.api.section.repository;

import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByWorkspaceAndIsDeleted(Workspace workspace, IsDeleted isDeleted);
}
