package com.example.coconote.api.block.entity;

import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Block extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canvas_id")
    private Canvas canvas;

    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_block_id")
    private Block nextBlock; // 순서를 알기 위한, 동레벨의 다음 블록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    private Block parentBlock;
}
