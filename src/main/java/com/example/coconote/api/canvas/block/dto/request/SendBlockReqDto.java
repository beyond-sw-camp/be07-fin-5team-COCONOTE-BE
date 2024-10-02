package com.example.coconote.api.canvas.block.dto.request;

import com.example.coconote.api.canvas.block.entity.Method;
import com.example.coconote.api.canvas.block.entity.Type;
import lombok.Data;

@Data
public class SendBlockReqDto {
    private Method method;
    private Long canvasId;
    private String prevBlockId;
    private String parentBlockId;
    private String contents;
    private Type type;
    private String feId;
    private String member;

    public CreateBlockReqDto buildCreateBlockReqDto() {
        return CreateBlockReqDto.builder()
                .canvasId(this.canvasId)
                .contents(this.contents)
                .type(this.type)
                .feId(this.feId)
                .prevBlockId(this.prevBlockId)
                .parentBlockId(this.parentBlockId)
                .member(this.member)
                .build();
    }

//    현재 create와 update 형식 동일
    public UpdateBlockReqDto buildUpdateBlockReqDto() {
        return UpdateBlockReqDto.builder()
                .canvasId(this.canvasId)
                .contents(this.contents)
                .type(this.type)
                .feId(this.feId)
                .prevBlockId(this.prevBlockId)
                .parentBlockId(this.parentBlockId)
                .member(this.member)
                .build();
    }
}
