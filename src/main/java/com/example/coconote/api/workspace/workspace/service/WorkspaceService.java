package com.example.coconote.api.workspace.workspace.service;

import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public WorkspaceListResDto workspaceCreate(WorkspaceCreateReqDto dto) {

        String imgUrl = "";
        // 이미지파일 저장하고 String 이미지URL로 바꾸는 코드

        Workspace workspace = dto.toEntity(imgUrl);

        Section sectionDefault = Section.builder()
                .sectionName("기본")
                .workspace(workspace)
                .build();
        Section sectionBookmark = Section.builder()
                .sectionName("즐겨찾기")
                .workspace(workspace)
                .build();
        workspace.getSections().add(sectionDefault);
        workspace.getSections().add(sectionBookmark);

        workspaceRepository.save(workspace);
        WorkspaceListResDto resDto = workspace.fromEntity();
        return resDto;
    }


    public List<WorkspaceListResDto> workspaceList() {
        List<Workspace> workspaces = workspaceRepository.findByIsDeleted(IsDeleted.N);
        List<WorkspaceListResDto> dtos = new ArrayList<>();
        for(Workspace w : workspaces) {
            dtos.add(w.fromEntity());
        }
        return dtos;
    }

    public WorkspaceListResDto workspaceUpdate(Long id, WorkspaceUpdateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        workspace.updateEntity(dto);
        WorkspaceListResDto resDto = workspace.fromEntity();
        return resDto;
    }

    public void workspaceDelete(Long id) {
        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        workspace.deleteEntity();
    }
}
