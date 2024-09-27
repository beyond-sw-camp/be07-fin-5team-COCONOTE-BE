package com.example.coconote.api.canvas.canvas.dto.response;

import com.example.coconote.api.channel.dto.response.ChannelResDto;
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

    private CanvasListResDto parentCanvas;
    private ChannelResDto channel;

//    ⭐ 추후 블록 내용 추가

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
