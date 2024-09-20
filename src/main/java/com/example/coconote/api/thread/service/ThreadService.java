package com.example.coconote.api.thread.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.repository.ThreadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final MemberRepository memberRepository;

    public Thread createThread(ThreadCreateReqDto dto) {
        //TODO: jwt토큰이 완성되면 memberId 는 불러오면됨
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()-> new EntityNotFoundException("해당멤버가 없습니다."));
        Thread parentThread = threadRepository.findById(dto.getParentId()).orElseThrow(()->new EntityNotFoundException("해당 쓰레드가 없습니다."));
        Thread thread = threadRepository.save(dto.toEntity(member,parentThread));
        return thread;
    }
}
