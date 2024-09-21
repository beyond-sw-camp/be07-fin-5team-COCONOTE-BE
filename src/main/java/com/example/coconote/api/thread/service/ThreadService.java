package com.example.coconote.api.thread.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.chat.ThreadRequestDto;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.repository.ThreadRepository;
import com.example.coconote.api.threadTag.repository.ThreadTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final ThreadTagRepository threadTagRepository;
    private final KafkaTemplate<String, ThreadRequestDto> kafkaTemplate;

    public ThreadResDto createThread(ThreadCreateReqDto dto) {
        //TODO: jwt토큰이 완성되면 memberId 는 불러오면됨
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()-> new EntityNotFoundException("해당멤버가 없습니다."));
        Thread parentThread = threadRepository.findById(dto.getParentId()).orElse(null);
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("해당 채널이 없습니다."));
        Thread thread = threadRepository.save(dto.toEntity(member,parentThread, channel));
        return thread.fromEntity();
    }

    public Page<ThreadResDto> threadList(Long channelId, Pageable pageable) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("channel not found"));
        Page<Thread> threads = threadRepository.findAllByChannelAndParentIsNull(channel,pageable);
        Page<ThreadResDto> threadListResDtos = threads.map(thread -> {
            List<Thread> childThreads = threadRepository.findAllByParent(thread);
            List<ThreadResDto> childThreadResDtos = childThreads.stream().map(Thread::fromEntity).toList();
            return thread.fromEntity(childThreadResDtos);
        });
        return threadListResDtos;
    }

    @Transactional
    public Thread createChatThread(ThreadRequestDto request) {
        Thread thread = Thread.builder()
                .content(request.getContent())
                .files(request.getFiles())
                .member(memberRepository.findById(request.getMemberId()).orElse(null))
                .channel(channelRepository.findById(request.getChannelId()).orElse(null))
                .build();

        if (request.getParentId() != null) {
            Thread parentThread = threadRepository.findById(request.getParentId()).orElse(null);
            thread = Thread.builder()
                    .parent(parentThread)
                    .build();
        }

        Thread savedThread = threadRepository.save(thread);

        // Kafka로 메시지 전송 전에 로그 찍기
        log.info("카프카에 보내기전 메세지 : {}", request);
        // Kafka로 메시지 전송
        kafkaTemplate.send("chat_topic", savedThread.getChannel().getId().toString(), request);

        return savedThread;
    }

    public List<ThreadResDto> getThreads(Long channelId) {
        List<Thread> threads = threadRepository.findByChannelId(channelId);
        return threads.stream().map(Thread::toDto).toList();
    }
}
