package com.example.coconote.api.workspace.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Workspace workspaceCreate(WorkspaceCreateReqDto dto) {
        Workspace workspace = dto.toEntity();
        workspaceRepository.save(workspace);

        return workspace;
    }
}
