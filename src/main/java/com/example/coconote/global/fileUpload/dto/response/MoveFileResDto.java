package com.example.coconote.global.fileUpload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveFileResDto {
    private Long fileId;
    private Long folderId;
    private String fileName;
    private String createMemberName;
    private Long channelId;
}
