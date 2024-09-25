package com.example.coconote.api.block.dto.response;

import com.example.coconote.api.block.entity.Block;
import com.example.coconote.api.block.entity.Type;
import com.example.coconote.api.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.entity.Canvas;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockListResDto {
    private Long id;
    private String content;

    private String feId; // front 종속 id
    private Type type;

    private String member;

    @Builder.Default
    private List<BlockListResDto> childBlock = new ArrayList<>();
}
