package com.example.coconote.api.canvas.entity;

import com.example.coconote.api.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Canvas extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_member_id")
    private Member createMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_canvas_id")
    private Canvas parentCanvas;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    public CanvasListResDto fromListEntity() {
        return CanvasListResDto.builder()
                .id(this.id)
                .title(this.title)
                .build();
    }

    public CanvasDetResDto fromDetEntity() {
        return CanvasDetResDto.builder()
                .id(this.id)
                .title(this.title)
                .parentCanvas(this.getParentCanvas())
                .channel(this.getChannel())
//               ⭐ 추후 블록 추가
                .createdTime(this.getCreatedTime())
                .updatedTime(this.getUpdatedTime())
                .build();
    }
}
