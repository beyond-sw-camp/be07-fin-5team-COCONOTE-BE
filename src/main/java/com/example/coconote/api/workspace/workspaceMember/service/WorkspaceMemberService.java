package com.example.coconote.api.workspace.workspaceMember.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberUpdateReqDto;
import com.example.coconote.common.IsDeleted;
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
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    @Autowired
    public WorkspaceMemberService(WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository, MemberRepository memberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
    }


    public WorkspaceMemberResDto workspaceMemberCreate(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }

        Member member = getMemberByEmail(email);
        if(member.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        // 이미 워크스페이스 회원일 때 예외
        if(workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).isPresent()) {
            throw new IllegalArgumentException("이미 워크스페이스에 가입되어 있는 회원입니다.");
        }

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .member(member)
                .nickname(member.getNickname())
                .build();
        workspaceMemberRepository.save(workspaceMember);
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

    public WorkspaceMemberResDto workspaceMemberUpdate(Long id, WorkspaceMemberUpdateReqDto dto) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        workspaceMember.updateEntity(dto);
        WorkspaceMemberResDto restDto = workspaceMember.fromEntity();
        return restDto;
    }


    public Boolean workspaceMemberChangeRole(Long id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        return workspaceMember.changeRole();
    }

    public void workspaceMemberDelete(Long id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        workspaceMember.deleteEntity();
    }

    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
    }
}
