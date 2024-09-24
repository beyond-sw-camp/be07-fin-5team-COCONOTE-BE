package com.example.coconote.api.channel.channel.entity;

import com.example.coconote.api.channel.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelResDto;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long channelId;

    private String channelName;

    private String channelInfo;

    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<ChannelMember> channelMembers = new ArrayList<>();

    // 폴더들과의 관계 (일대다 관계)
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<Folder> folders;

    // 캔버스 관계
//    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
//    private List<Canvas> canvas;

    public ChannelListResDto fromEntity(Section section) {
        return ChannelListResDto.builder()
                .sectionId(section.getSectionId())
                .channelId(this.channelId)
                .channelName(this.channelName)
                .channelInfo(this.channelInfo)
                .isPublic(this.isPublic)
                .build();
    }
    public ChannelResDto fromEntity() {
        return ChannelResDto.builder()
                .channelId(this.channelId)
                .build();
    }

    public ChannelResDto toResDto() {
        return ChannelResDto.builder()
                .channelId(this.channelId)
                .build();
    }

    public void updateEntity(ChannelUpdateReqDto dto) {
        this.channelName = dto.getChannelName();
        this.channelInfo = dto.getChannelInfo();
        this.isPublic = dto.getIsPublic();
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
        if(this.channelMembers != null) {
            for(ChannelMember c : this.channelMembers) {
                c.deleteEntity();
            }
        }
    }
}

