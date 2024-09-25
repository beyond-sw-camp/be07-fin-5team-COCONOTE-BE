package com.example.coconote.api.block.dto.request;

import com.example.coconote.api.block.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBlockReqDto {
//    private Long blockId; // Attr의 feId로 판별 예정
    private Long canvasId;

    private Long prevBlockId;
    private Long parentBlockId;
    private String contents;

    //    front 종속 값
    private Type type;
    private String feId;
}
