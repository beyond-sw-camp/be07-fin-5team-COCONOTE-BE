package com.example.coconote.api.thread.threadTag.service;

import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.tag.repository.TagRepository;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.thread.repository.ThreadRepository;
import com.example.coconote.api.thread.threadTag.dto.ThreadTagReqDto;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import com.example.coconote.api.thread.threadTag.repository.ThreadTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ThreadTagService {

    private final ThreadTagRepository threadTagRepository;
    private final ThreadRepository ThreadRepository;
    private final TagRepository tagRepository;

    public void addThreadTag(ThreadTagReqDto dto) {
        Thread thread = ThreadRepository.findById(dto.getThreadId()).get();
        Tag tag = tagRepository.findById(dto.getTagId()).get();
        threadTagRepository.save(new ThreadTag(thread, tag));
    }

    public void deleteThreadTag(Long id) {
//        ThreadTag threadTag = findThreadTag(id);
//        ThreadTagRepository.delete(threadTag);
        threadTagRepository.deleteById(id);
    }
    public ThreadResDto deleteThreadTag(ThreadReqDto dto) {
        log.info("dto.getThreadTagId() {}", dto.getThreadTagId());
        threadTagRepository.deleteById(dto.getThreadTagId());
        return ThreadResDto.builder()
                .type(MessageType.REMOVE_TAG)
                .id(dto.getThreadId())
                .tagId(dto.getTagId())
                .build();
    }
    private ThreadTag findThreadTag(Long id) {
        return threadTagRepository.findById(id).orElseThrow(()->new EntityNotFoundException("threadTag not found"));
    }


}
