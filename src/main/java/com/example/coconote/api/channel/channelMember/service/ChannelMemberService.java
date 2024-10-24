package com.example.coconote.api.channel.channelMember.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.entity.ChannelType;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.dto.request.ChannelMemberRoleReqDto;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.security.util.CustomPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ChannelMemberService {

    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public ChannelMemberService(ChannelMemberRepository channelMemberRepository,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                ChannelRepository channelRepository,
                                SectionRepository sectionRepository,
                                MemberRepository memberRepository,
                                WorkspaceRepository workspaceRepository) {

        this.channelMemberRepository = channelMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.channelRepository = channelRepository;
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
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


        ChannelMember channelMemberDeleted = channelMemberRepository.findByChannelAndWorkspaceMember(channel, workspaceMember).orElse(
                ChannelMember.builder()
                        .workspaceMember(workspaceMember)
                        .channel(channel)
                        .build());

        if (channelMemberDeleted.getIsDeleted().equals(IsDeleted.Y)) {
            channelMemberDeleted.restoreEntity();
            return channelMemberDeleted.fromEntity();
        };

        if(workspaceMember.getWsRole().equals(WsRole.PMANAGER) || workspaceMember.getWsRole().equals(WsRole.SMANAGER)) {
            channelMemberDeleted.changeRole(ChannelRole.MANAGER);
        }
        channelMemberRepository.save(channelMemberDeleted);
        return channelMemberDeleted.fromEntity();

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

    public ChannelMemberListResDto channelMemberChangeRole(ChannelMemberRoleReqDto dto, String email) {
        ChannelMember channelMember = channelMemberRepository.findById(dto.getId()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
//      채널멤버 =>  채널, 워크스페이스 멤버
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        if (channelMember.getWorkspaceMember().getWsRole() == WsRole.SMANAGER || channelMember.getWorkspaceMember().getWsRole() == WsRole.PMANAGER) {
            throw new IllegalArgumentException("워크스페이스 관리자는 변경할 수 없습니다.");
        }
        WorkspaceMember workspaceMember = channelMember.getWorkspaceMember();
        Workspace workspace = workspaceMember.getWorkspace();
        Channel channel = channelMember.getChannel();
        Member myMember = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자의 Member 정보를 찾을 수 없습니다."));
        WorkspaceMember myWorkspaceMember = workspaceMemberRepository.findByMemberAndWorkspace(myMember, workspace).orElseThrow(() -> new EntityNotFoundException("사용자의 Workspace 정보를 찾을 수 없습니다."));
        ChannelMember myChannelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, myWorkspaceMember, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("사용자의 ChannelMember 정보를 찾을 수 없습니다."));
        if (myChannelMember.getChannelRole().equals(ChannelRole.USER)) {
            throw new IllegalArgumentException("변경 권한이 없습니다.");
        }
        if (myChannelMember.equals(channelMember)) {
            throw new IllegalArgumentException("변경 권한이 없습니다.");
        }
        return channelMember.changeRole(dto.getChannelRole());
    }

    public Boolean channelBookmark(Long channelId, String email) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()-> new EntityNotFoundException("채널을 찾을 수 없습니다."));
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        return channelMember.bookmarkMyChannel();
    }

    public void channelMemberDelete(Long id, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        ChannelMember channelMember = channelMemberRepository.findByIdAndIsDeleted(id, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
        Channel channel = channelMember.getChannel();
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        ChannelMember deleter = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
        if(deleter.getChannelRole().equals(ChannelRole.USER)) {
            throw new IllegalArgumentException("회원을 강퇴시킬 권한이 없습니다.");
        }
        if (workspaceMember.getWsRole() == WsRole.SMANAGER || workspaceMember.getWsRole() == WsRole.PMANAGER) {
            throw new IllegalArgumentException("워크스페이스 관리자는 강퇴할 수 없습니다.");
        }
        if(channel.getChannelType().equals(ChannelType.DEFAULT)) {
            throw new IllegalArgumentException("기본 채널에서는 다른 회원을 퇴장시킬 수 없습니다.");
        }
        if (deleter.equals(channelMember)) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다.");
        }
        channelMember.deleteEntity();
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
        if(channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.Y).isPresent()) {
            ChannelMember channelMemberCameBack = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.Y).orElseThrow(()-> new EntityNotFoundException("없는 채널 회원입니다."));
            channelMemberCameBack.restoreEntity();
            return channelMemberCameBack.fromEntity();
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

    public void channelMemberLeave(Long channelId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 조회할 수 없습니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(()-> new EntityNotFoundException("채널을 조회할 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스 회원입니다."));
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 조회할 수 없습니다."));
        if(channel.getChannelType().equals(ChannelType.DEFAULT)) {
            throw new IllegalArgumentException("기본 채널에서는 나갈 수 없습니다.");
        }
        channelMember.deleteEntity();
    }
}
