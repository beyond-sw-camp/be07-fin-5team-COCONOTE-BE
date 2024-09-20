package com.example.coconote.api.channelMember.service;

import com.example.coconote.api.channelMember.entity.ChannelMember;
import com.example.coconote.api.channelMember.repository.ChannelMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ChannelMemberService {

    private final ChannelMemberRepository channelMemberRepository;
    @Autowired
    public ChannelMemberService(ChannelMemberRepository channelMemberRepository) {
        this.channelMemberRepository = channelMemberRepository;
    }


//    public ChannelMember channelMemberCreate(ChannelMemberCreateReqDto dto) {
//
//        ChannelMember channelMember = dto.toEntity();
//        channelMemberRepository.save(channelMember);
//
//        return channelMember;
//    }
//
//    public List<ChannelMemberListResDto> channelMemberList() {
//        List<ChannelMember> channelMembers = channelMemberRepository.findAll();
//        List<ChannelMemberListResDto> dtos = new ArrayList<>();
//        for(ChannelMember c : channelMembers) {
//            dtos.add(c.fromEntity());
//        }
//        return dtos;
//    }
//
//    public ChannelMember channelMemberUpdate(Long id, ChannelMemberUpdateReqDto dto) {
//        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException(" 찾을 수 없습니다."));
//        channelMember.updateEntity(dto);
//        return channelMember;
//    }
//
//    public void channelMemberDelete(Long id) {
//        ChannelMember channelMember = channelMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
//        channelMember.deleteEntity();
//    }
}
