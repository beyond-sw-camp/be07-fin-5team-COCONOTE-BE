package com.example.coconote.api.channel.channelMember.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
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
    private final ChannelRepository channelRepository;

    @Autowired
    public ChannelMemberService(ChannelMemberRepository channelMemberRepository, ChannelRepository channelRepository) {
        this.channelMemberRepository = channelMemberRepository;
        this.channelRepository = channelRepository;
    }

    public ChannelMemberListResDto channelMemberCreate(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("채널이 존재하지 않습니다."));
        ChannelMember channelMember = ChannelMember
                .builder()
                .channel(channel)
                .build();
        channelMemberRepository.save(channelMember);
        ChannelMemberListResDto resDto = channelMember.fromEntity();

        return resDto;
    }

    public List<ChannelMemberListResDto> channelMemberList(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("채널이 존재하지 않습니다."));
        List<ChannelMember> channelMembers = channelMemberRepository.findByChannelAndIsDeleted(channel, IsDeleted.N);
        List<ChannelMemberListResDto> resDtos = new ArrayList<>();

        for (ChannelMember c : channelMembers) {
            resDtos.add(c.fromEntity());
        }
        return resDtos;
    }

    public Boolean channelMemberChangeRole(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        return channelMember.changeRole();
    }

    public Boolean channelBookmark(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(" 찾을 수 없습니다."));
        return channelMember.bookmarkMyChannel();
    }

    public void channelMemberDelete(Long id) {
        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        channelMember.deleteEntity();
    }
}
