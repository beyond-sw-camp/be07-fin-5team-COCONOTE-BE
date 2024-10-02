package com.example.coconote.api.section.entity;

import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
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

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sectionId;

    private String sectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "section", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SectionType sectionType = SectionType.GENERAL;

//    @Builder.Default
//    private Boolean isExpanded = false;

    public SectionListResDto fromEntity(Member member) {
        List<ChannelDetailResDto> cList = new ArrayList<>();
//        List<ChannelDetailResDto> dtos = new ArrayList<>();
        if(this.channels != null) {
            for(Channel c : this.channels) {
                List<ChannelMember> cMembers = c.getChannelMembers();
                for (ChannelMember cm : cMembers) {
                    if (c.getIsPublic() || cm.getWorkspaceMember().getMember().equals(member)) { // 비공개채널이고 내가 채널멤버도 아님 -> continue
                        cList.add(c.fromEntity(this));
                    }
                }
            }
        }

        return SectionListResDto.builder()
                .sectionId(this.sectionId)
                .sectionName(this.sectionName)
                .channelList(cList)
                .build();
    }

    public void updateEntity(SectionUpdateReqDto dto) {
        this.sectionName = dto.getSectionName();
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
        if(this.channels != null) {
            for (Channel c : this.channels) {
                c.deleteEntity();
            }
        }
    }
}