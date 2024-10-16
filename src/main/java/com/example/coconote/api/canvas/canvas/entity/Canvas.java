package com.example.coconote.api.canvas.canvas.entity;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.search.entity.CanvasBlockDocument;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "prev_canvas_id")
//    순서를 알기 위한, 동레벨의 이전 캔버스
//    이전 캔버스이 없다면, 최상위 캔버스!
    private Canvas prevCanvas;

    @OneToMany(mappedBy = "parentCanvas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Canvas> childCanvasList;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    // 소프트 삭제 메서드
    public void markAsDeleted() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();

        // 재귀적으로 삭제 처리
        if (childCanvasList != null) {
            for (Canvas child : childCanvasList) {
                child.markAsDeleted();
            }
        }
    }

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
                .parentCanvas(this.getParentCanvas() != null ? this.getParentCanvas().fromListEntity() : null)
                .channel(this.getChannel() != null ? this.getChannel().fromEntity() : null)
//               ⭐ 추후 블록 추가
                .createdTime(this.getCreatedTime())
                .updatedTime(this.getUpdatedTime())
                .build();
    }

    public void updateInfo(String title, Canvas parentCanvas, IsDeleted isDeleted) {
        this.title = title;
        this.parentCanvas = parentCanvas;
        this.isDeleted = isDeleted;
    }

    public void changePrevCanvas(Canvas canvas) {
        this.prevCanvas = canvas;
    }

    public CanvasBlockDocument fromBlockDocEntity() {
        return CanvasBlockDocument.builder()
                .canvasTitle(this.title)
                .createMemberName(this.createMember.getNickname())
                .channelId(this.channel.getChannelId())
                .canvasCreatedTime(this.getCreatedTime().toString())
                .build();
    }
}
