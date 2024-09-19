package com.example.coconote.api.section.service;

import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class SectionService {

    private final SectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    @Autowired
    public SectionService(SectionRepository sectionRepository, WorkspaceRepository workspaceRepository) {
        this.sectionRepository = sectionRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public Section sectionCreate(SectionCreateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));

        Section section = dto.toEntity(workspace);
        sectionRepository.save(section);

        return section;
    }


    public List<SectionListResDto> sectionList() {
        List<Section> sections = sectionRepository.findAll();
        List<SectionListResDto> dtos = new ArrayList<>();
        for(Section s : sections) {
            dtos.add(s.fromEntity());
        }
        return dtos;

    }

    public Section sectionUpdate(Long id, SectionUpdateReqDto dto) {
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("section not found"));
        section.updateEntity(dto);
        return section;
    }
}
