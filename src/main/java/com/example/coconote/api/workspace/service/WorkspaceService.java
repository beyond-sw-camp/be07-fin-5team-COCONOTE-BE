package com.example.coconote.api.workspace.service;

import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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

        // 기본섹션, 즐겨찾기 섹션 생성 코드

        workspaceRepository.save(workspace);
        return workspace;
    }


    public List<WorkspaceListResDto> workspaceList() {
        List<Workspace> workspaces = workspaceRepository.findAll();
        List<WorkspaceListResDto> dtos = new ArrayList<>();
        for(Workspace w : workspaces) {
            dtos.add(w.fromEntity());
        }
        return dtos;
    }
}
