package com.example.coconote.api.workspace.workspaceMember.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.search.repository.WorkspaceMemberSearchRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberCreateReqDto;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberUpdateReqDto;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceMemberSearchRepository workspaceMemberSearchRepository;
    private final WorkspaceMemberMapper workspaceMemberMapper;



    @Transactional
    public WorkspaceMemberResDto workspaceMemberCreate(WorkspaceMemberCreateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId()).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(member.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        WorkspaceMember workspaceMember = dto.toEntity(workspace, member);
        workspaceMemberRepository.save(workspaceMember);

        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        workspaceMemberSearchRepository.save(document);  // ElasticSearch에 인덱싱

        return workspaceMember.fromEntity();
    }

    public List<WorkspaceMemberResDto> workspaceMemberList(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<WorkspaceMemberResDto> dtos = new ArrayList<>();
        for(WorkspaceMember w : workspaceMembers) {
            dtos.add(w.fromEntity());
        }
        return dtos;
    }

    @Transactional
    public WorkspaceMemberResDto workspaceMemberUpdate(Long id, WorkspaceMemberUpdateReqDto dto) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        workspaceMember.updateEntity(dto);

        workspaceMemberRepository.save(workspaceMember);
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        workspaceMemberSearchRepository.save(document);  // ElasticSearch에 인덱싱

        WorkspaceMemberResDto restDto = workspaceMember.fromEntity();
        return restDto;
    }


    @Transactional
    public Boolean workspaceMemberChangeRole(Long id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }

        workspaceMemberRepository.save(workspaceMember);
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        workspaceMemberSearchRepository.save(document);  // ElasticSearch에 인덱싱

        return workspaceMember.changeRole();
    }

    @Transactional
    public void workspaceMemberDelete(Long id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }

        workspaceMember.deleteEntity();
        workspaceMemberSearchRepository.deleteById(String.valueOf(workspaceMember.getWorkspaceMemberId()));  // ElasticSearch에서 삭제
    }
}
