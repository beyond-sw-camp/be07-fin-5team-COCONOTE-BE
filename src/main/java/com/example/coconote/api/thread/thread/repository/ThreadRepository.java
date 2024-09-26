package com.example.coconote.api.thread.thread.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

//    Page<Thread> findAllByChannelAndParentIsNullOrderByCreatedTimeDesc(Channel channel, Pageable pageable);
    Page<Thread> findAllByChannelAndIsDeletedAndParentIsNullOrderByCreatedTimeDesc(Channel channel, IsDeleted isDeleted, Pageable pageable);

    List<Thread> findAllByParentAndIsDeleted(Thread thread, IsDeleted isDeleted);
}
