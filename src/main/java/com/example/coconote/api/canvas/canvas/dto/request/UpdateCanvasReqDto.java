package com.example.coconote.api.canvas.canvas.dto.request;

import com.example.coconote.common.IsDeleted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCanvasReqDto {
    private String title;
    private Long parentCanvasId;
    private Long prevCanvasId;
    private Long canvasId;
    private Long channelId;
    @Builder.Default
    private IsDeleted isDeleted = IsDeleted.N;
}
