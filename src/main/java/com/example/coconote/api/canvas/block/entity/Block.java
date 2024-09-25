package com.example.coconote.api.canvas.block.entity;

import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
    @JoinColumn(name = "prev_block_id")
//    순서를 알기 위한, 동레벨의 이전 블록
//    이전 블록이 없다면, 최상위 블록!
    private Block prevBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    private Block parentBlock;

    public void changePrevBlock(Block block) {
        this.prevBlock = block;
    }

    public void updateAllInfo(Block prevBlock, Block parentBlock, String contents) {
        if(this.prevBlock != null && !Objects.equals(this.prevBlock.getId(), prevBlock.getId())){
            this.prevBlock = prevBlock;
        }

        if(this.parentBlock != null && !Objects.equals(this.parentBlock.getId(), parentBlock.getId())){
            this.parentBlock = parentBlock;
        }

        if(!Objects.equals(this.contents, contents)){
            this.contents = contents;
        }
    }

    public BlockListResDto fromEntity() {
        return BlockListResDto.builder()
                .id(this.id)
                .content(this.contents)
                .build();
    }
}
