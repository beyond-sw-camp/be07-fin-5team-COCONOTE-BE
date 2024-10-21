package com.example.coconote.api.canvas.block.entity;

import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
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

    @Column(length = 5000)
    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_member_id")
    private WorkspaceMember workspaceMember;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
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
    private Integer indent; // front tap 기능을 위해 추가

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

    public void updateAllInfo(Block prevBlock, Block parentBlock, String contents, Integer level, Integer indent) {
        if(this.prevBlock == null || (this.prevBlock != null && !Objects.equals(this.prevBlock.getId(), prevBlock.getId()))){
            this.prevBlock = prevBlock;
        }

        if(this.parentBlock != null && !Objects.equals(this.parentBlock.getId(), parentBlock.getId())){
            this.parentBlock = parentBlock;
        }

        if(!Objects.equals(this.contents, contents)){
            this.contents = contents;
        }

        if(!Objects.equals(this.level, level) && level != null && level > 0){
            this.level = level;
        }

        if(!Objects.equals(this.indent, indent)){
            this.indent = indent;
        }
    }

    public BlockListResDto fromEntity() {
        return BlockListResDto.builder()
                .feId(this.feId)
                .type(this.getType())
                .content(this.contents)
                .workspaceMember(this.workspaceMember)
                .prevBlockFeId(this.prevBlock != null ? this.prevBlock.getFeId() : null) // 이전 블록의 feId 설정
                .build();
    }

    public Block copy() {
        return Block.builder()
                .id(this.id)
                .canvas(this.canvas)
                .contents(this.contents)
                .workspaceMember(this.workspaceMember)
                .prevBlock(this.prevBlock)
                .parentBlock(this.parentBlock)
                .type(this.type)
                .level(this.level)
                .feId(this.feId)
                .build();
    }
}
