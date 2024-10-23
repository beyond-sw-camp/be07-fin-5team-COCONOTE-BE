package com.example.coconote.api.thread.threadTag.service;

import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.search.mapper.ThreadMapper;
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
import org.springframework.kafka.core.KafkaTemplate;
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
    private final ThreadMapper threadMapper;
    private final KafkaTemplate kafkaTemplate;


    public void addThreadTag(ThreadTagReqDto dto) {
        Thread thread = ThreadRepository.findById(dto.getThreadId()).get();
        Tag tag = tagRepository.findById(dto.getTagId()).get();
        threadTagRepository.save(new ThreadTag(thread, tag));
        ThreadDocument document = threadMapper.toDocument(thread, thread.getWorkspaceMember().getProfileImage());  // toDocument로 미리 변환
        IndexEntityMessage<ThreadDocument> indexEntityMessage = new IndexEntityMessage<>(thread.getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.THREAD, document);
        kafkaTemplate.send("thread_entity_search", indexEntityMessage.toJson());
    }

    public void deleteThreadTag(Long id) {
//        ThreadTag threadTag = findThreadTag(id);
//        ThreadTagRepository.delete(threadTag);
        threadTagRepository.deleteById(id);
    }
    public ThreadResDto deleteThreadTag(ThreadReqDto dto) {
        log.info("dto.getThreadTagId() {}", dto.getThreadTagId());
        threadTagRepository.deleteById(dto.getThreadTagId());
        Thread thread = ThreadRepository.findById(dto.getThreadId()).orElseThrow(()-> new EntityNotFoundException("Thread not found"));
        return ThreadResDto.builder()
                .type(MessageType.REMOVE_TAG)
                .id(dto.getThreadId())
                .tagId(dto.getTagId())
                .parentThreadId(thread.getParent() != null ? thread.getParent().getId() : null)
                .build();
    }
    private ThreadTag findThreadTag(Long id) {
        return threadTagRepository.findById(id).orElseThrow(()->new EntityNotFoundException("threadTag not found"));
    }


}
