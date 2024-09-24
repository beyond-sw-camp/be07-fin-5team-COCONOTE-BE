package com.example.coconote.api.channel.channelMember.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByChannelAndIsDeleted(Channel channel, IsDeleted isDeleted);
}
