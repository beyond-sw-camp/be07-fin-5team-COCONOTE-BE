package com.example.coconote.api.channelMember.repository;

import com.example.coconote.api.channelMember.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

}
