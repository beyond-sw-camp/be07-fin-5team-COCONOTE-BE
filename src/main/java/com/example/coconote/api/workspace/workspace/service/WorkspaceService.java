package com.example.coconote.api.workspace.workspace.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channel.service.ChannelService;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.service.ChannelMemberService;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.search.service.SearchService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final SectionRepository sectionRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelMemberService channelMemberService;
    private final SearchService searchService;
    private final ChannelService channelService;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Transactional
    public WorkspaceListResDto workspaceCreate(WorkspaceCreateReqDto dto, String email) {
        // 이미지파일 저장하고 String 이미지URL로 바꾸는 코드
        String imgUrl = "";
        Workspace workspace = dto.toEntity(imgUrl);
        workspaceRepository.save(workspace);

        // 기본섹션 생성
        Section sectionDefault = Section.builder()
                .sectionName("기본")
                .workspace(workspace)
                .sectionType(SectionType.DEFAULT)
                .build();
        sectionRepository.save(sectionDefault);
        workspace.getSections().add(sectionDefault);

        // 기본 채널 생성
        Channel channelDefault = Channel.builder()
                .section(sectionDefault)
                .channelName("일반")
                .channelInfo("일반 채널입니다.")
                .isPublic(true)
                .build();
        Channel channelNotice = Channel.builder()
                .section(sectionDefault)
                .channelName("공지사항")
                .channelInfo("공지사항 채널입니다.")
                .isPublic(true)
                .build();
        channelService.createDefaultFolder(channelDefault);
        channelService.createDefaultFolder(channelNotice);
        channelRepository.save(channelDefault);
        channelRepository.save(channelNotice);
        sectionDefault.getChannels().add(channelDefault);
        sectionDefault.getChannels().add(channelNotice);

        // 워크스페이스 멤버로 영입(?)
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .member(member)
                .nickname(member.getNickname())
                .wsRole(WsRole.PMANAGER)
                .build();
        workspaceMemberRepository.save(workspaceMember);
        workspace.getWorkspaceMembers().add(workspaceMember);

        ChannelMemberListResDto channelMemberDefault = channelMemberService.channelMemberCreate(channelDefault.getChannelId(), email);
        channelMemberService.channelMemberChangeRole(channelMemberDefault.getId());
        ChannelMemberListResDto channelMemberNotice = channelMemberService.channelMemberCreate(channelNotice.getChannelId(), email);
        channelMemberService.channelMemberChangeRole(channelMemberNotice.getId());

        workspaceRepository.save(workspace);

        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        IndexEntityMessage<WorkspaceMemberDocument> indexEntityMessage = new IndexEntityMessage<>(workspaceMember.getWorkspace().getWorkspaceId(), EntityType.WORKSPACE_MEMBER , document);
        kafkaTemplate.send("workspace_member_entity_search", indexEntityMessage.toJson());

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

    public WorkspaceListResDto workspaceInfo(Long workspaceId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("워크스페이스 정보를 볼 수 있는 권한이 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        return workspace.fromEntity();
    }

    public List<SectionListResDto> workspaceDetail(Long workspaceId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<SectionListResDto> sDtos = new ArrayList<>();
        for(Section s : sections) {
            sDtos.add(s.fromEntity(member));
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

    public WorkspaceListResDto workspaceFirst(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByMemberAndIsDeleted(member, IsDeleted.N);
        return workspaceMembers.get(0).getWorkspace().fromEntity();
    }


}
