package com.example.coconote.api.search.entity;

import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasBlockDocument {
    @Id
    private Long id;
    private String type;  // "canvas" 또는 "block"

    // Canvas 관련 필드
    private Long canvasId;
    private String canvasTitle;
    private String createMemberName;
    private Long channelId;
    private String  canvasCreatedTime;

    // Block 관련 필드
    private Long blockId;
    private String blockContents;
    private Long workspaceMemberId;
    private String prevBlockId;
    private String parentBlockId;
    private String feId;
    private String blockType;
}
