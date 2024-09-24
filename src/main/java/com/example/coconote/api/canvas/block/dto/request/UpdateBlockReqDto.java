package com.example.coconote.api.canvas.block.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBlockReqDto {
    private Long blockId;
    private Long prevBlockId;
    private Long parentBlockId;
    private String contents;
}
