package com.example.coconote.api.section.service;

import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
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
public class SectionService {

    private final SectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    @Autowired
    public SectionService(SectionRepository sectionRepository, WorkspaceRepository workspaceRepository) {
        this.sectionRepository = sectionRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public SectionListResDto sectionCreate(SectionCreateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));

        Section section = dto.toEntity(workspace);
        sectionRepository.save(section);

        return section.fromEntity();
    }


    public List<SectionListResDto> sectionList(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<SectionListResDto> dtos = new ArrayList<>();
        for(Section s : sections) {
            dtos.add(s.fromEntity());
        }
        return dtos;
    }

    public SectionListResDto sectionUpdate(Long id, SectionUpdateReqDto dto) {
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("찾을 수 없습니다.");
        }
        section.updateEntity(dto);
        SectionListResDto resDto = section.fromEntity();
        return resDto;
    }


    public void sectionDelete(Long id) {
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("section not found"));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("찾을 수 없습니다.");
        }
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElse(null);
        section.deleteEntity();
        workspaceRepository.save(workspace);
    }
}
