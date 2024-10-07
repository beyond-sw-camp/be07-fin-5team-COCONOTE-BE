package com.example.coconote.api.thread.threadFile.service;

import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadFile.repository.ThreadFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ThreadFileService {
    private final ThreadFileRepository threadFileRepository;

    public ThreadResDto deleteThreadFile(ThreadReqDto threadReqDto) {
        ThreadFile threadFile = threadFileRepository.findByFileID(threadReqDto.getFileId()).orElseThrow(()->new EntityNotFoundException("threadFile not found"));
        threadFile.markAsDeleted();
        return ThreadResDto.builder()
                .id(threadReqDto.getThreadId())
                .type(MessageType.DELETE_FILE)
                .fileId(threadFile.getFileID())
                .build();
    }

    public void deleteFile(Long fileId) {
        ThreadFile threadFile = threadFileRepository.findByFileID(fileId).orElseThrow(()->new EntityNotFoundException("threadFile not found"));
        threadFile.markAsDeleted();
    }
}
