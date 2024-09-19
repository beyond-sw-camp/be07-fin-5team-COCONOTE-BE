package com.example.coconote.api.block.entity;

import com.example.coconote.api.canvas.entity.Canvas;
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
public class Block extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canvas_id")
    private Canvas canvas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    private Block parentBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    private Block nextBlock;

}
