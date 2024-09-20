package com.example.coconote.api.threadTag.service;

import com.example.coconote.api.tag.entity.Tag;
import com.example.coconote.api.tag.repository.TagRepository;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.repository.ThreadRepository;
import com.example.coconote.api.threadTag.dto.ThreadTagAddReqDto;
import com.example.coconote.api.threadTag.entity.ThreadTag;
import com.example.coconote.api.threadTag.repository.ThreadTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ThreadTagService {

    private final ThreadTagRepository ThreadTagRepository;
    private final ThreadRepository ThreadRepository;
    private final TagRepository tagRepository;

    public void addThreadTag(ThreadTagAddReqDto dto) {
        Thread thread = ThreadRepository.findById(dto.getThreadId()).get();
        Tag tag = tagRepository.findById(dto.getTagId()).get();
        ThreadTagRepository.save(new ThreadTag(thread, tag));
    }
}
