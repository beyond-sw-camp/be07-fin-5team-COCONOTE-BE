package com.example.coconote.api.channelMember.repository;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channelMember.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByChannel(Channel channel);
}
