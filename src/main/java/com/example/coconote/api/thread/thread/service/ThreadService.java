package com.example.coconote.api.thread.thread.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.thread.repository.ThreadRepository;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadFile.repository.ThreadFileRepository;
import com.example.coconote.api.thread.threadTag.repository.ThreadTagRepository;
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
    private final ThreadFileRepository threadFileRepository;
    private final SearchService searchService;

    public ThreadResDto createThread(ThreadReqDto dto) {
        //TODO: jwt토큰이 완성되면 memberId 는 불러오면됨
        Member member = memberRepository.findById(dto.getSenderId()).orElseThrow(()-> new EntityNotFoundException("해당멤버가 없습니다."));
        Thread parentThread = null;
        if(dto.getParentId() != null){
            parentThread = threadRepository.findById(dto.getParentId()).orElse(null);
        }
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("해당 채널이 없습니다."));

        Thread thread = threadRepository.save(dto.toEntity(member,parentThread, channel));

//        검색
        searchService.indexThread(channel.getSection().getWorkspace().getWorkspaceId(), thread);

        if(dto.getFiles() != null){
            for (ThreadFileDto threadFileDto : dto.getFiles()){
                threadFileRepository.save(threadFileDto.toEntity(thread));
            }
        }
        return thread.fromEntity();
    }

    public Page<ThreadResDto> threadList(Long channelId, Pageable pageable) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("channel not found"));
        Page<Thread> threads = threadRepository.findAllByChannelAndParentIsNullOrderByCreatedTimeDesc(channel,pageable);
        Page<ThreadResDto> threadResDtos = threads.map(thread -> {
            List<Thread> childThreads = threadRepository.findAllByParent(thread);
            List<ThreadResDto> childThreadResDtos = childThreads.stream().map(Thread::fromEntity).toList();
            List<ThreadFileDto> threadFileDtos = thread.getThreadFiles().stream().map(ThreadFile::fromEntity).toList();
            return thread.fromEntity(childThreadResDtos,threadFileDtos);
        });
        return threadResDtos;
    }

    @Transactional
    public ThreadResDto deleteThread(Long threadId) {
        Thread thread = threadRepository.findById(threadId).orElseThrow(()->new EntityNotFoundException("thread not found"));
//        isDeleted를 true로 바꾸는 것으로 대체
        thread.markAsDeleted();
        searchService.deleteThread(thread.getChannel().getSection().getWorkspace().getWorkspaceId(), String.valueOf(thread.getId()));
        return ThreadResDto.builder()
                .id(thread.getId())
                .type(MessageType.DELETE)
                .build();
    }

    public ThreadResDto updateThread(ThreadReqDto threadReqDto) {
        Thread thread = threadRepository.findById(threadReqDto.getThreadId()).orElseThrow(()->new EntityNotFoundException("thread not found"));
        thread.updateThread(threadReqDto);
        searchService.indexThread(thread.getChannel().getSection().getWorkspace().getWorkspaceId(), thread);
        return thread.fromEntity(MessageType.UPDATE);
    }
}
