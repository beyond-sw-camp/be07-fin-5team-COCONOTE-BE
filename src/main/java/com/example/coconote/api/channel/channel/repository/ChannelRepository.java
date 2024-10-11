package com.example.coconote.api.channel.channel.repository;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByIsDeleted(IsDeleted isDeleted);
    List<Channel> findBySectionAndIsDeleted(Section section, IsDeleted isDeleted);

    @Query("SELECT c FROM Channel c " +
            "LEFT JOIN c.channelMembers cm " +  // 채널과 채널 멤버를 LEFT JOIN으로 연결
            "WHERE c.section = :section " +  // 해당 섹션의 채널만 가져오기
            "AND c.isDeleted = :isDeleted " +  // 삭제되지 않은 채널만
            "AND (c.isPublic = true OR cm.workspaceMember = :workspaceMember)")  // 공개 채널이거나, 워크스페이스 멤버가 속한 비공개 채널만
    List<Channel> findChannelsByWorkspaceMemberOrPublic(Section section, IsDeleted isDeleted, WorkspaceMember workspaceMember);
}
