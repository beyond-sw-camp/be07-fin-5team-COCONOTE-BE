package com.example.coconote.api.canvas.block.entity;

import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
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

    //    ⭐ 추후 로그인 붙일 때 변경
    private String member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prev_block_fe_id")
//    순서를 알기 위한, 동레벨의 이전 블록
//    이전 블록이 없다면, 최상위 블록!
    private Block prevBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_fe_id")
    private Block parentBlock;

//    (프론트 tiptap 종속) 블록의 타입
    @Enumerated(EnumType.STRING)
    private Type type;

    private Integer level; // front 태그에 level이 필요한 경우 사용

    @Column(unique = true)
    private String feId; // 프론트에서 적용해주는 uuid 형식의 block id

    // 소프트 삭제 메서드
    public void markAsDeleted(List<Block> parentLinkedChildrenBlocks) {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();

        // 재귀적으로 삭제 처리
        if (parentLinkedChildrenBlocks != null) {
            for (Block child : parentLinkedChildrenBlocks) {
                child.markAsDeleted(null);
            }
        }
    }

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
            log.info(this.getContents());
            log.info("!!!!!!!!!!");
            log.info(contents);
            this.contents = contents;
        }
    }

    public BlockListResDto fromEntity() {
        return BlockListResDto.builder()
                .feId(this.feId)
                .type(this.getType())
                .content(this.contents)
                .member(this.member)
                .build();
    }

}
