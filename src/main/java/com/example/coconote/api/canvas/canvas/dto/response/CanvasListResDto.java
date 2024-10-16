package com.example.coconote.api.canvas.canvas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanvasListResDto {
    private Long id;
    private String title;

    @Builder.Default
    private List<CanvasListResDto> childCanvas = new ArrayList<>();
    private Long prevCanvasId;
}
