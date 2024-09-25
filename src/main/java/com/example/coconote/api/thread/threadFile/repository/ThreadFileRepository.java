package com.example.coconote.api.thread.threadFile.repository;

import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadFileRepository extends JpaRepository<ThreadFile, Long> {
}
