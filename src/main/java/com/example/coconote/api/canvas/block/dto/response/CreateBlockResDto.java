package com.example.coconote.api.canvas.block.dto.response;

import com.example.coconote.api.canvas.block.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBlockResDto {
    private Long blockId;

    private Long canvasId;
    private Long nextBlockId;
    private Long parentBlockId;

    private String contents;

    public static CreateBlockResDto fromEntity(Block block) {
        return CreateBlockResDto.builder()
                .blockId(block.getId())
                .canvasId(block.getCanvas().getId())
                .nextBlockId(block.getPrevBlock() == null ? null : block.getPrevBlock().getId())
                .parentBlockId(block.getParentBlock() == null ? null : block.getParentBlock().getId())
                .contents(block.getContents())
                .build();
    }
}
