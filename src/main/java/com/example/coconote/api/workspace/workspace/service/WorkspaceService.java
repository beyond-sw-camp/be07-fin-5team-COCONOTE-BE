package com.example.coconote.api.workspace.workspace.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.entity.SectionType;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
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
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final SectionRepository sectionRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository, SectionRepository sectionRepository, MemberRepository memberRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceRepository = workspaceRepository;
        this.sectionRepository = sectionRepository;
        this.memberRepository = memberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public WorkspaceListResDto workspaceCreate(WorkspaceCreateReqDto dto, String email) {

        String imgUrl = "";
        // 이미지파일 저장하고 String 이미지URL로 바꾸는 코드

        Workspace workspace = dto.toEntity(imgUrl);

        Section sectionDefault = Section.builder()
                .sectionName("기본")
                .workspace(workspace)
                .sectionType(SectionType.DEFAULT)
                .build();
        Section sectionBookmark = Section.builder()
                .sectionName("즐겨찾기")
                .workspace(workspace)
                .sectionType(SectionType.BOOKMARK)
                .build();
        workspace.getSections().add(sectionDefault);
        workspace.getSections().add(sectionBookmark);


        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .member(member)
                .nickname(member.getNickname())
                .wsRole(WsRole.PMANAGER)
                .build();

        workspaceMemberRepository.save(workspaceMember);
        workspace.getWorkspaceMembers().add(workspaceMember);
        workspaceRepository.save(workspace);
        return workspace.fromEntity();
    }


    public List<WorkspaceListResDto> workspaceList(String email) {

        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByMemberAndIsDeleted(member, IsDeleted.N);
        List<WorkspaceListResDto> dtos = new ArrayList<>();
        if(workspaceMembers != null) {
            for (WorkspaceMember w : workspaceMembers) {
                Workspace workspace = w.getWorkspace();
                dtos.add(workspace.fromEntity());
            }
        }
        return dtos;
    }

    public List<SectionListResDto> workspaceDetail(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<SectionListResDto> sDtos = new ArrayList<>();
        for(Section s : sections) {
            sDtos.add(s.fromEntity());
        }

        return sDtos;
    }

    public WorkspaceListResDto workspaceUpdate(Long id, WorkspaceUpdateReqDto dto) {
        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        workspace.updateEntity(dto);
        WorkspaceListResDto resDto = workspace.fromEntity();
        return resDto;
    }

    public void workspaceDelete(Long id) {
        Workspace workspace = workspaceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        workspace.deleteEntity();
    }

}
