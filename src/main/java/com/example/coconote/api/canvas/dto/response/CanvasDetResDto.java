package com.example.coconote.api.canvas.dto.response;

import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.channel.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanvasDetResDto {
    private Long id;
    private String title;

    private Canvas parentCanvas;
    private Channel channel;

//    ⭐ 추후 블록 내용 추가

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
