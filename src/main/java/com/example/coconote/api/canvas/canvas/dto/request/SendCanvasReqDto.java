package com.example.coconote.api.canvas.canvas.dto.request;


import com.example.coconote.api.canvas.block.entity.Method;
import lombok.Data;

@Data
public class SendCanvasReqDto {
    private Long channelId;
    private Long parentCanvasId = null;
    private Long prevCanvasId = null;
    private Long nextCanvasId = null;
    private Method method;
    private Long canvasId;
    private String title = null;
    private String member;

    public CreateCanvasReqDto buildCreateCanvasReqDto() {
        return CreateCanvasReqDto.builder()
                .parentCanvasId(this.parentCanvasId)
                .channelId(this.channelId)
                .title(this.title)
                .build();
    }

    public UpdateCanvasReqDto buildUpdateCanvasReqDto() {
        return UpdateCanvasReqDto.builder()
                .title(this.title)
                .parentCanvasId(this.parentCanvasId)
                .canvasId(this.canvasId)
                .channelId(this.channelId)
                .build();
    }

    public ChangeOrderCanvasReqDto buildChangeOrderCanvasReqDto() {
        return ChangeOrderCanvasReqDto.builder()
                .canvasId(this.canvasId)
                .member(this.member)
                .prevCanvasId(this.prevCanvasId)
                .nextCanvasId(this.nextCanvasId)
                .parentCanvasId(this.parentCanvasId)
                .build();
    }
}
