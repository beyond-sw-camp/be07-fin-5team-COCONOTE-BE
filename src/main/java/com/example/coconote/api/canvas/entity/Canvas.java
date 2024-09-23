package com.example.coconote.api.canvas.entity;

import com.example.coconote.api.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
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

    @OneToMany(mappedBy = "parentCanvas", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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
}
