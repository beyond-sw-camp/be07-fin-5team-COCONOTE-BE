package com.example.coconote.api.block.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBlockReqDto {
    private Long canvasId;
    private Long nextBlockId;
    private Long parentBlockId;

    private String contents;
}
