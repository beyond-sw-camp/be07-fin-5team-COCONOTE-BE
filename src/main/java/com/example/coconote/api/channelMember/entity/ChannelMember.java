package com.example.coconote.api.channelMember.entity;

import com.example.coconote.api.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChannelRole channelRole = ChannelRole.USER; // 최초 가입 시에는 그냥 일반 유저

    @Builder.Default
    private Boolean isBookmark = true; // 최초 가입 시에는 즐겨찾기 자동으로 등록돼 있음

    public ChannelMemberListResDto fromEntity() {
        return ChannelMemberListResDto.builder()
                .id(this.id)
                .channelRole(this.channelRole)
                .build();
    }
}