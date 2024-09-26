package com.example.coconote.api.thread.threadFile.repository;

import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThreadFileRepository extends JpaRepository<ThreadFile, Long> {
    Optional<ThreadFile> findByFileID(Long fileID);

}
