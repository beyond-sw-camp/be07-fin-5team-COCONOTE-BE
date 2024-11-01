package com.example.coconote.api.thread.threadFile.service;

import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.search.mapper.ThreadMapper;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.thread.repository.ThreadRepository;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadFile.repository.ThreadFileRepository;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThreadFileService {
    private final ThreadFileRepository threadFileRepository;
    private final ThreadMapper threadMapper; // ThreadDocument 생성 시 사용
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ThreadRepository threadRepository;

    public ThreadResDto deleteThreadFile(ThreadReqDto threadReqDto) {
        // 1. 파일 조회
        ThreadFile threadFile = threadFileRepository.findByFileID(threadReqDto.getFileId())
                .orElseThrow(() -> new EntityNotFoundException("threadFile not found"));

        // 2. 파일 소프트 삭제
        threadFile.markAsDeleted();

        // 3. 파일이 속한 쓰레드 조회
        Thread thread = threadRepository.findById(threadReqDto.getThreadId())
                .orElseThrow(() -> new EntityNotFoundException("Thread not found"));

        // 4. 검색 인덱스에서 파일 삭제를 반영하여 업데이트
        updateSearchIndex(thread);

        // 5. 응답 생성
        return ThreadResDto.builder()
                .id(threadReqDto.getThreadId())
                .type(MessageType.DELETE_FILE)
                .fileId(threadFile.getFileID())
                .channelId(thread.getChannel().getChannelId())
                .parentThreadId(thread.getParent() != null ? thread.getParent().getId() : null)
                .build();
    }

    private void updateSearchIndex(Thread thread) {
        // ThreadDocument 생성
        ThreadDocument document = threadMapper.toDocument(thread);

        // 삭제된 파일을 반영하여 fileUrls 업데이트
        List<String> updatedFileUrls = thread.getThreadFiles().stream()
                .filter(file -> !file.getIsDeleted().equals(IsDeleted.Y)) // 삭제되지 않은 파일만 필터링
                .map(ThreadFile::getFileURL)
                .toList();
        document.setFileUrls(updatedFileUrls); // 업데이트된 파일 URL 리스트 설정

        // 검색 인덱스 메시지 생성
        IndexEntityMessage<ThreadDocument> indexEntityMessage = new IndexEntityMessage<>(
                thread.getChannel().getSection().getWorkspace().getWorkspaceId(),
                EntityType.THREAD,
                document
        );

        // Kafka로 메시지 전송하여 검색 인덱스 업데이트
        kafkaTemplate.send("thread_entity_search", indexEntityMessage.toJson());
    }

    public void deleteFile(Long fileId) {
        ThreadFile threadFile = threadFileRepository.findByFileID(fileId).orElseThrow(()->new EntityNotFoundException("threadFile not found"));
        threadFile.markAsDeleted();
    }
}
