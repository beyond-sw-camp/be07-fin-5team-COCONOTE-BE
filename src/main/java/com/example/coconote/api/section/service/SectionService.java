package com.example.coconote.api.section.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.entity.SectionType;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
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
    private final MemberRepository memberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    public SectionService(SectionRepository sectionRepository, WorkspaceRepository workspaceRepository, MemberRepository memberRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.sectionRepository = sectionRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public SectionListResDto sectionCreate(SectionCreateReqDto dto, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        if(workspaceMember.getWsRole().equals(WsRole.USER)) {
            throw new IllegalArgumentException("섹션을 생성할 수 있는 권한이 없습니다.");
        }
        Section section = dto.toEntity(workspace);
        sectionRepository.save(section);

        return section.fromEntity(member);
    }


    public List<SectionListResDto> sectionList(Long workspaceId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<SectionListResDto> dtos = new ArrayList<>();
        for(Section s : sections) {
            dtos.add(s.fromEntity(member));
        }
        return dtos;
    }

    public SectionListResDto sectionUpdate(Long id, SectionUpdateReqDto dto, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        if(!checkWorkspaceAuthorization(id, email)) {
            throw new IllegalArgumentException("섹션을 수정할 수 있는 권한이 없습니다.");
        }
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        section.updateEntity(dto);
        SectionListResDto resDto = section.fromEntity(member);
        return resDto;
    }


    public void sectionDelete(Long id, String email) {
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        if(!checkWorkspaceAuthorization(id, email)) {
            throw new IllegalArgumentException("섹션을 삭제할 수 있는 권한이 없습니다.");
        }
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        if(section.getSectionType().equals(SectionType.DEFAULT)) {
            throw new IllegalArgumentException("기본 섹션은 삭제할 수 없습니다.");
        }
        section.deleteEntity();
    }

    private Boolean checkWorkspaceAuthorization(Long SectionId, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(SectionId).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        return !workspaceMember.getWsRole().equals(WsRole.USER);
    }
}
