package com.example.coconote.api.block.dto.request;


import com.example.coconote.api.block.entity.Type;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private String contents;

//    ⭐ 추후 로그인 붙일 때 변경
    private String member;
    
//    front 종속 값
    private Type type;
    private String feId;
    private String prevBlockId;
    private String parentBlockId;
}
