package com.example.coconote.api.canvas.block.dto.response;

import com.example.coconote.api.canvas.block.entity.Type;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockListResDto {
    private Long id;
    private String content;

    private String feId; // front 종속 id
    private Type type;

    private WorkspaceMember workspaceMember;

    @Builder.Default
    private List<BlockListResDto> childBlock = new ArrayList<>();

    private String prevBlockFeId; // 이전 블록의 feId

}

