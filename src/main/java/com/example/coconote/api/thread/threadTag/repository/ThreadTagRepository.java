package com.example.coconote.api.thread.threadTag.repository;

import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadTagRepository extends JpaRepository<ThreadTag, Long> {
}
