package com.example.coconote.api.thread.thread.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

//    Page<Thread> findAllByChannelAndParentIsNullOrderByCreatedTimeDesc(Channel channel, Pageable pageable);
    Page<Thread> findAllByChannelAndIsDeletedAndParentIsNullOrderByCreatedTimeDesc(Channel channel, IsDeleted isDeleted, Pageable pageable);

    List<Thread> findAllByParentAndIsDeleted(Thread thread, IsDeleted isDeleted);

    @Query("SELECT COUNT(t) FROM Thread t WHERE t.channel = ?1 AND t.parent IS NULL AND t.id >= ?2 AND t.isDeleted = ?3")
    Long countByChannelAndParentIsNullAndIdGreaterThanEqual(Channel channel, Long id, IsDeleted isDeleted);

//    Long countByChannelAndParentIsNull(Channel channel, Long id);

    @Query("SELECT t FROM Thread t LEFT JOIN FETCH t.childThreads LEFT JOIN FETCH t.threadFiles " +
            "WHERE t.channel = :channel AND t.isDeleted = :isDeleted AND t.parent IS NULL " +
            "ORDER BY t.createdTime DESC")
    Page<Thread> findThreadsWithChildrenAndFilesByChannelAndIsDeleted(Channel channel, IsDeleted isDeleted, Pageable pageable);
}
