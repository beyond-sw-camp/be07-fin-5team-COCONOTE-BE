package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasBlockSearchResultDto {
    private String id;
    private String canvasTitle;
    private String blockContents;
}
