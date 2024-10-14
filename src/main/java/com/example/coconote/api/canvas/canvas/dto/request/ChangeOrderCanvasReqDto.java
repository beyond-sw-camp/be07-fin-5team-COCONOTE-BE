package com.example.coconote.api.canvas.canvas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeOrderCanvasReqDto {
    private Long id;

    //    ⭐ 추후 로그인 붙일 때 변경
    private String member;

    private Long prevCanvasId; // 현 Canvas의 이전 블록 > 현 블록"이" prevCanvas으로 참조하고 있는 블록
    private Long nextCanvasId; // 현 Canvas의 다음 블록 > 현 블록"을" prevCanvasId로 참조하고 있는 블록

    private Long parentCanvasId; // 현재 사용 X
}
