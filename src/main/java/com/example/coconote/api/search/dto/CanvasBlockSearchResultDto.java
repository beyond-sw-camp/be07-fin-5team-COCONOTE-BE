package com.example.coconote.api.search.dto;

import com.example.coconote.api.canvas.block.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasBlockSearchResultDto {
    private String type;
    private Long canvasId;
    private String canvasTitle;
    private Long blockId;
    private String blockContents;
    private Long channelId;
    private String channelName;
}
