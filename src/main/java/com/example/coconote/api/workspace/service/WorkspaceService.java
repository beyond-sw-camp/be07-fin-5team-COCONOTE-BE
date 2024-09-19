package com.example.coconote.api.workspace.service;

import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Workspace workspaceCreate(WorkspaceCreateReqDto dto, MultipartFile imgFile) {

        String imgUrl = "";
        // 이미지파일 저장하고 String 이미지URL로 바꾸는 코드

        Workspace workspace = dto.toEntity(imgUrl);
        workspaceRepository.save(workspace);
        return workspace;
    }
}
