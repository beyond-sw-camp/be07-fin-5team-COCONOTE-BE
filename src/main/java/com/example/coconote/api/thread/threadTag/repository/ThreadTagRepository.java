package com.example.coconote.api.thread.threadTag.repository;

import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadTagRepository extends JpaRepository<ThreadTag, Long> {
    List<ThreadTag> findAllByTagIdIn(List<Long> tagSearchIds);

    @Query("SELECT tt.thread FROM ThreadTag tt " +
            "JOIN tt.tag t " +
            "WHERE t.id IN :tagIds " +
            "AND tt.thread.channel.channelId = :channelId " +
            "GROUP BY tt.thread " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount")
    List<Thread> findThreadsByChannelAndAllTagIds(
            @Param("channelId") Long channelId,
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") Long tagCount);

    List<ThreadTag> findByTag_Id(Long id);
}
