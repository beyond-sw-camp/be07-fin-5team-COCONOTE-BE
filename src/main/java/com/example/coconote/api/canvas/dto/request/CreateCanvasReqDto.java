package com.example.coconote.api.canvas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCanvasReqDto {
    private String title;
    private Long parentCanvasId;
    private Long channelId;

}
