package com.example.coconote.api.section.service;

import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionSwitchReqDto;
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
import java.util.stream.Collectors;

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
        for (Section s : sections) {
            // 삭제되지 않은 채널만 리스트에 추가
            List<ChannelDetailResDto> filteredChannels = s.getChannels().stream()
                    .filter(channel -> channel.getIsDeleted().equals(IsDeleted.N))
                    .map(channel -> channel.fromEntity(s))  // DTO 변환
                    .collect(Collectors.toList());

            dtos.add(SectionListResDto.builder()
                    .sectionId(s.getSectionId())
                    .sectionName(s.getSectionName())
                    .channelList(filteredChannels)
                    .build());
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

//    public SectionListResDto switchIndex(SectionSwitchReqDto dto, String email) {
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
//        Section section = sectionRepository.findById(dto.getSectionId()).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
//        List<Channel> channels = section.getChannels();
//        Long fromIndex = dto.getFromIndex();
//        Long toIndex = dto.getToIndex();
//
//        if (fromIndex < 0 || fromIndex >= channels.size() || toIndex < 0 || toIndex >= channels.size() || fromIndex == toIndex) {
//            throw new IllegalArgumentException("잘못된 접근입니다.");
//        }
//        Channel channelToMove = channels.get(Math.toIntExact(fromIndex));
//
//        // 이동할 요소를 원래 위치에서 빈 자리를 만든 후 이동
//        if (fromIndex < toIndex) {
//            for (int i = Math.toIntExact(fromIndex); i < toIndex; i++) {
//                channels.set(i, channels.get(i + 1));
//            }
//        } else {
//            for (int i = Math.toIntExact(fromIndex); i > toIndex; i--) {
//                channels.set(i, channels.get(i - 1));
//            }
//        }
//
//        channels.set(Math.toIntExact(toIndex), channelToMove); // 새로운 위치에 요소 추가
//
//        return section.fromEntity(member);
//
//    }

    private Boolean checkWorkspaceAuthorization(Long SectionId, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(SectionId).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        return !workspaceMember.getWsRole().equals(WsRole.USER);
    }

}
