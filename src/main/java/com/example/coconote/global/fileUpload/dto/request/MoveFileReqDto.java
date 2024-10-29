package com.example.coconote.global.fileUpload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveFileReqDto {
    private Long fileId;
    private Long folderId;
}
