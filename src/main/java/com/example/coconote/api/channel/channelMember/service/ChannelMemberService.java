package com.example.coconote.api.channel.channelMember.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.entity.Section;
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
import java.util.Objects;

@Service
@Transactional
public class ChannelMemberService {

    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final SectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ChannelMemberService(ChannelMemberRepository channelMemberRepository,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                ChannelRepository channelRepository,
                                SectionRepository sectionRepository,
                                WorkspaceRepository workspaceRepository,
                                MemberRepository memberRepository) {
        this.channelMemberRepository = channelMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.channelRepository = channelRepository;
        this.sectionRepository = sectionRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
    }

    public ChannelMemberListResDto channelMemberCreate(Long channelId, String email) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("채널이 존재하지 않습니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스를 탈퇴한 회원입니다.");
        }
        if(channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).isPresent()) {
            throw new IllegalArgumentException("이미 채널에 가입되어 있는 회원입니다.");
        }
        ChannelMember channelMember = ChannelMember.builder()
                .workspaceMember(workspaceMember)
                .channel(channel)
                .build();
        channelMemberRepository.save(channelMember);
        return channelMember.fromEntity();
    }

    public List<ChannelMemberListResDto> channelMemberList(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("채널이 존재하지 않습니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        List<ChannelMember> channelMembers = channelMemberRepository.findByChannelAndIsDeleted(channel, IsDeleted.N);
        List<ChannelMemberListResDto> resDtos = new ArrayList<>();

        for (ChannelMember c : channelMembers) {
            resDtos.add(c.fromEntity());
        }
        return resDtos;
    }

    public Boolean channelMemberChangeRole(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        return channelMember.changeRole();
    }

    public Boolean channelBookmark(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        return channelMember.bookmarkMyChannel();
    }

    public void channelMemberDelete(Long id, String email) {
        if(!checkChannelAuthorization(id, email)) {
            throw new IllegalArgumentException("");
        };
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        channelMember.deleteEntity();
    }

    private Boolean checkChannelAuthorization(Long channelId, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("채널을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(channel.getSection().getSectionId()).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
        return channelMember.getChannelRole().equals(ChannelRole.MANAGER);
    }

    @Transactional
    public ChannelMemberListResDto channelMemberInvite(Long channelId, Long workspaceMemberId, String email) {
//        자기 자신 검증
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        WorkspaceMember selfWorkspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(selfWorkspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        if(channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, selfWorkspaceMember, IsDeleted.N).isEmpty()) {
            throw new IllegalArgumentException("채널에 가입되어 있지 않은 회원입니다.");
        }
//        초대할 회원 검증
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(workspaceMemberId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        if(channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).isPresent()) {
            throw new IllegalArgumentException("이미 채널에 가입되어 있는 회원입니다.");
        }
//        같은 워크스페이스의 회원만 초대 가능
        if (!Objects.equals(selfWorkspaceMember.getWorkspace().getWorkspaceId(), workspaceMember.getWorkspace().getWorkspaceId())) {
            throw new IllegalArgumentException("같은 워크스페이스의 회원만 초대할 수 있습니다.");
        }
        ChannelMember channelMember = ChannelMember.builder()
                .channel(channel)
                .workspaceMember(workspaceMember)
                .build();
        channelMemberRepository.save(channelMember);
        return channelMember.fromEntity();
    }

}
