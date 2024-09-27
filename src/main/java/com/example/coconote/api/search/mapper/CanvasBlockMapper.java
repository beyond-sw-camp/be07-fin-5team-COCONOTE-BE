package com.example.coconote.api.search.mapper;

import com.example.coconote.api.search.entity.CanvasBlockDocument;
import org.springframework.stereotype.Component;
import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.canvas.entity.Canvas;

@Component
public class CanvasBlockMapper {

    // Canvas를 UnifiedDocument로 변환
    public CanvasBlockDocument toDocument(Canvas canvas) {
        return CanvasBlockDocument.builder()
                .id("canvas-" + canvas.getId())
                .type("canvas")
                .canvasTitle(canvas.getTitle())
                .createMemberName(canvas.getCreateMember().getNickname())
                .channelId(canvas.getChannel().getChannelId())
                .canvasCreatedTime(canvas.getCreatedTime().toString())
                .build();
    }

    // Block을 UnifiedDocument로 변환
    public CanvasBlockDocument toDocument(Block block) {
        return CanvasBlockDocument.builder()
                .id("block-" + block.getId())
                .type("block")
                .blockContents(block.getContents())
                .blockMember(block.getMember())
                .canvasId(String.valueOf(block.getCanvas().getId()))
                .prevBlockId(block.getPrevBlock() != null ? String.valueOf(block.getPrevBlock().getId()) : null)
                .parentBlockId(block.getParentBlock() != null ? String.valueOf(block.getParentBlock().getId()) : null)
                .feId(block.getFeId())
                .blockType(block.getType().toString())
                .build();
    }
}
