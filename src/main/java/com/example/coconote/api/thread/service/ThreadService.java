package com.example.coconote.api.thread.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.repository.ThreadRepository;
import com.example.coconote.api.threadTag.repository.ThreadTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final ThreadTagRepository threadTagRepository;

    public ThreadResDto createThread(ThreadCreateReqDto dto) {
        //TODO: jwt토큰이 완성되면 memberId 는 불러오면됨
        Member member = memberRepository.findById(dto.getSenderId()).orElseThrow(()-> new EntityNotFoundException("해당멤버가 없습니다."));
        Thread parentThread = null;
        if(dto.getParentId() != null){
            parentThread = threadRepository.findById(dto.getParentId()).orElse(null);
        }
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("해당 채널이 없습니다."));
        Thread thread = threadRepository.save(dto.toEntity(member,parentThread, channel));
        return thread.fromEntity();
    }

    public Page<ThreadResDto> threadList(Long channelId, Pageable pageable) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("channel not found"));
        Page<Thread> threads = threadRepository.findAllByChannelAndParentIsNull(channel,pageable);
        Page<ThreadResDto> threadResDtos = threads.map(thread -> {
            List<Thread> childThreads = threadRepository.findAllByParent(thread);
            List<ThreadResDto> childThreadResDtos = childThreads.stream().map(Thread::fromEntity).toList();
            return thread.fromEntity(childThreadResDtos);
        });
        return threadResDtos;
    }

    @Transactional
    public void deleteThread(Long threadId) {
        Thread thread = threadRepository.findById(threadId).orElseThrow(()->new EntityNotFoundException("thread not found"));
//        isDeleted를 true로 바꾸는 것으로 대체
        thread.markAsDeleted();
    }
}
