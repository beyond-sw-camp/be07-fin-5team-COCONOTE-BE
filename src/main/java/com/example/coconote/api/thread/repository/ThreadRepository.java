package com.example.coconote.api.thread.repository;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.thread.entity.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    Page<Thread> findAllByChannel(Channel channel, Pageable pageable);
    List<Thread> findAllByParent(Thread thread);
}
