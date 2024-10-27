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
                .canvasId(canvas.getId())
                .type("canvas")
                .canvasTitle(canvas.getTitle())
                .createMemberName(canvas.getWorkspaceMember().getNickname())
                .channelId(canvas.getChannel().getChannelId())
                .canvasCreatedTime(canvas.getCreatedTime().toString())
                .build();
    }

    // Block을 UnifiedDocument로 변환
    public CanvasBlockDocument toDocument(Block block) {
        return CanvasBlockDocument.builder()
                .canvasId(block.getCanvas().getId())
                .type("block")
                .blockId(block.getId())
                .blockContents(block.getContents())
                .workspaceMemberId(block.getWorkspaceMember().getWorkspaceMemberId())
                .prevBlockId(block.getPrevBlock() != null ? String.valueOf(block.getPrevBlock().getId()) : null)
                .parentBlockId(block.getParentBlock() != null ? String.valueOf(block.getParentBlock().getId()) : null)
                .feId(block.getFeId())
                .blockType(block.getType().toString())
                .build();
    }
}
