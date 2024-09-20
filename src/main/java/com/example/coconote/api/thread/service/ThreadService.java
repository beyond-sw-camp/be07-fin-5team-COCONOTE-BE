package com.example.coconote.api.thread.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadListResDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.repository.ThreadRepository;
import com.example.coconote.api.threadTag.entity.ThreadTag;
import com.example.coconote.api.threadTag.repository.ThreadTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final ThreadTagRepository threadTagRepository;

    public Thread createThread(ThreadCreateReqDto dto) {
        //TODO: jwt토큰이 완성되면 memberId 는 불러오면됨
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()-> new EntityNotFoundException("해당멤버가 없습니다."));
        Thread parentThread = threadRepository.findById(dto.getParentId()).orElseThrow(()->new EntityNotFoundException("해당 쓰레드가 없습니다."));
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("해당 채널이 없습니다."));
        Thread thread = threadRepository.save(dto.toEntity(member,parentThread, channel));
        return thread;
    }

    public Page<ThreadListResDto> threadList(Long channelId,Pageable pageable) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("channel not found"));
        Page<Thread> threads = threadRepository.findAllByChannel(channel,pageable);
        Page<ThreadListResDto> threadListResDtos = threads.map(thread -> {
            List<Thread> childThreads = threadRepository.findAllByParent(thread);
            List<ThreadListResDto> childThreadListResDtos = childThreads.stream().map(Thread::fromEntity).toList();
            return thread.fromEntity(childThreadListResDtos);
        });
        return threadListResDtos;
    }
}
