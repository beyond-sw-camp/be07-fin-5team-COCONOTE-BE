package com.example.coconote.api.channel.entity;

import com.example.coconote.api.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.drive.entity.Folder;
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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Channel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long channelId;

    private String channelName;

    private String channelInfo;

    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

//    @OneToMany(mappedBy = "channel", cascade = CascadeType.PERSIST)
//    @Builder.Default
//    private List<ChannelMember> channelMembers = new ArrayList<>();

    // 폴더들과의 관계 (일대다 관계)
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<Folder> folders;

    public ChannelListResDto fromEntity() {
        return ChannelListResDto.builder()
                .name(this.channelName)
                .info(this.channelInfo)
                .build();
    }

    public void updateEntity(ChannelUpdateReqDto dto) {
        this.channelName = dto.getChannelName();
        this.channelInfo = dto.getChannelInfo();
        this.isPublic = dto.isPublic();
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }
}