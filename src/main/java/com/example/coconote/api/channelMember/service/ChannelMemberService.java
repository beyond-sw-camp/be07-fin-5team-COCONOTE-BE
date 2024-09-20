//package com.example.coconote.api.channelMember.service;
//
//import com.example.coconote.api.channel.entity.Channel;
//import com.example.coconote.api.channel.repository.ChannelRepository;
//import com.example.coconote.api.channelMember.dto.response.ChannelMemberListResDto;
//import com.example.coconote.api.channelMember.entity.ChannelMember;
//import com.example.coconote.api.channelMember.repository.ChannelMemberRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChannelMemberService {
//
//    private final ChannelMemberRepository channelMemberRepository;
//    private final ChannelRepository channelRepository;
//    @Autowired
//    public ChannelMemberService(ChannelMemberRepository channelMemberRepository, ChannelRepository channelRepository) {
//        this.channelMemberRepository = channelMemberRepository;
//        this.channelRepository = channelRepository;
//    }
//
//    public ChannelMember channelMemberCreate(Long channelId) {
//        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("채널이 존재하지 않습니다."));
//        ChannelMember channelMember = ChannelMember
//                .builder()
//                .channel(channel)
//                .build();
//        channelMemberRepository.save(channelMember);
//
//        return channelMember;
//    }

//    public List<ChannelMemberListResDto> channelMemberList(Long channelId) {
//        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("채널이 존재하지 않습니다."));
//        List<ChannelMember> c = commentRepository.findByBoardAndDelYN(pageable, board, DelYN.N);
////        Page<CommentListResDto> commentListResDtos = comments.map(
////                a->a.fromEntity());
//        Page<CommentListResDto> commentListResDtos = comments.map(Comment::fromEntity);
//        return commentListResDtos;
//
//        return dtos;
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
//    }}

