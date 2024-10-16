package com.example.coconote.api.canvas.canvas.dto.request;

import com.example.coconote.api.canvas.block.entity.Type;
import com.example.coconote.api.canvas.canvas.entity.CanvasMessageMethod;
import com.example.coconote.api.canvas.canvas.entity.PostMessageType;
import com.example.coconote.common.IsDeleted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanvasSocketReqDto {
    private CanvasMessageMethod method;
    private PostMessageType postMessageType;
    private Long channelId;
    private Long senderId; // 메시지 보낸사람 id

//    공통 사용
    private IsDeleted isDeleted; // 삭제여부 확인

//    캔버스 용도
    private Long canvasId; // 방번호

    @Builder.Default
    private String canvasTitle = null; // 캔버스 명

    private Long parentCanvasId; // 현재 사용 X
    private Long prevCanvasId; // 현 Canvas의 이전 블록 > 현 블록"이" prevCanvas으로 참조하고 있는 블록
    private Long nextCanvasId; // 현 Canvas의 다음 블록 > 현 블록"을" prevCanvasId로 참조하고 있는 블록

//    블록 용도
    private Long blockId;
    private String blockFeId;
    private String prevBlockId;
    @Builder.Default
    private String nextBlockId = null; // Method.changeOrder 전용
    private String parentBlockId;
    private String blockContents;
    private Type blockType;

    public CreateCanvasReqDto buildCreateCanvasReqDto() {
        return CreateCanvasReqDto.builder()

                .build();
    }
}
