package com.example.coconote.api.canvas.block.dto.request;

import com.example.coconote.api.canvas.block.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeOrderBlockReqDto {
    private Long canvasId;

    //    ⭐ 추후 로그인 붙일 때 변경
    private String member;

    //    front 종속 값
    private String feId;
    private String prevBlockId; // 현 block의 이전 블록 > 현 블록"이" prevBlock으로 참조하고 있는 블록
    private String nextBlockId; // 현 block의 다음 블록 > 현 블록"을" prevBlockId로 참조하고 있는 블록


    private String parentBlockId; // 현재 사용 X
}
