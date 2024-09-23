package com.example.coconote.api.channel.channel.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByIsDeleted(IsDeleted isDeleted);
}
