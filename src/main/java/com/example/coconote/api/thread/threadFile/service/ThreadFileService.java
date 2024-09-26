package com.example.coconote.api.thread.threadFile.service;

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

    public void deleteThreadFile(Long fileId) {
        ThreadFile threadFile = threadFileRepository.findByFileID(fileId).orElseThrow(()->new EntityNotFoundException("threadFile not found"));
        threadFile.markAsDeleted();
    }
}
