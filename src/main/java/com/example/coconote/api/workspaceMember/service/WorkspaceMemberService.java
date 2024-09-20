package com.example.coconote.api.workspaceMember.service;

import com.example.coconote.api.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspaceMember.dto.request.WorkspaceMemberCreateReqDto;
import com.example.coconote.api.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.api.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspaceMember.repository.WorkspaceMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    public WorkspaceMemberService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public WorkspaceMemberResDto workspaceMemberCreate(WorkspaceMemberCreateReqDto dto) {

        WorkspaceMember workspaceMember = dto.toEntity();
        workspaceMemberRepository.save(workspaceMember);
        WorkspaceMemberResDto resDto = workspaceMember.fromEntity();
        return resDto;
    }

    public List<WorkspaceMemberResDto> workspaceMemberList() {
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findAll();
        List<WorkspaceMemberResDto> dtos = new ArrayList<>();
        for(WorkspaceMember w : workspaceMembers) {
            dtos.add(w.fromEntity());
        }
        return dtos;
    }


    public WorkspaceMemberResDto workspaceMemberUpdate(Long id, WorkspaceUpdateReqDto dto) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        workspaceMember.updateEntity(dto);
        WorkspaceMemberResDto resDto = workspaceMember.fromEntity();
        return resDto;
    }

    public void workspaceMemberDelete(Long id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        workspaceMember.deleteEntity();
    }
}
