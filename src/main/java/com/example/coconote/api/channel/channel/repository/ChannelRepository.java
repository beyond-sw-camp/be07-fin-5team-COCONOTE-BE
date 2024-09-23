package com.example.coconote.api.channel.channel.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {


}
