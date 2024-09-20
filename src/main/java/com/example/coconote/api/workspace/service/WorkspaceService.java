package com.example.coconote.api.workspace.service;

import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
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

        Section sectionDefault = Section.builder()
                .name("기본")
                .workspace(workspace)
                .build();
        Section sectionBookmark = Section.builder()
                .name("즐겨찾기")
                .workspace(workspace)
                .build();
        workspace.getSections().add(sectionDefault);
        workspace.getSections().add(sectionBookmark);

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

    public Workspace workspaceUpdate(Long id, WorkspaceUpdateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException(" 찾을 수 없습니다."));
        workspace.updateEntity(dto);
        return workspace;
    }

    public void workspaceDelete(Long id) {

        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        workspace.deleteEntity();
    }
}