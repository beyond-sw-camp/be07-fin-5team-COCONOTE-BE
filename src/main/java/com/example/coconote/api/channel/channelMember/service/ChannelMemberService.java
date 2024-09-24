package com.example.coconote.api.channel.channelMember.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.dto.request.ChannelMemberCreateReqDto;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
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
public class ChannelMemberService {

    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;

    @Autowired
    public ChannelMemberService(ChannelMemberRepository channelMemberRepository, WorkspaceMemberRepository workspaceMemberRepository, ChannelRepository channelRepository) {
        this.channelMemberRepository = channelMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.channelRepository = channelRepository;
    }

    public ChannelMemberListResDto channelMemberCreate(ChannelMemberCreateReqDto dto) {
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(() -> new EntityNotFoundException("채널이 존재하지 않습니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(dto.getWorkspaceMemberId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스를 탈퇴한 회원입니다.");
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

    public void channelMemberDelete(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        if(channelMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 채널을 탈퇴한 회원입니다.");
        }
        channelMember.deleteEntity();
    }
}
