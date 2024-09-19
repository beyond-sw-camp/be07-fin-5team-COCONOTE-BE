package com.example.coconote.api.drive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateFolderReqDto {
    private Long channelId;
    private Long parentFolderId;
}
