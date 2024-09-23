package com.example.coconote.api.canvas.dto.response;

import com.example.coconote.api.canvas.entity.Canvas;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCanvasResDto {
    private Long canvasId;
    private String title;
    private Long parentCanvasId;

    private Long channelId;

    public static CreateCanvasResDto fromEntity(Canvas canvas) {
        return CreateCanvasResDto.builder()
                .canvasId(canvas.getId())
                .title(canvas.getTitle())
                .parentCanvasId(canvas.getParentCanvas() == null ? null : canvas.getParentCanvas().getId())
                .channelId(canvas.getChannel() == null ? null : canvas.getChannel().getChannelId())
                .build();
    }
}
