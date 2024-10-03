package com.example.coconote.api.channel.channelMember.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByChannelAndIsDeleted(Channel channel, IsDeleted isDeleted);
    Optional<ChannelMember> findByChannelAndWorkspaceMemberAndIsDeleted(Channel channel, WorkspaceMember workspaceMember, IsDeleted isDeleted);
    List<ChannelMember> findByWorkspaceMemberAndIsDeleted(WorkspaceMember workspaceMember, IsDeleted isDeleted);
}
