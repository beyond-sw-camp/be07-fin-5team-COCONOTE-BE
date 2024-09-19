package com.example.coconote.api.drive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveFolderResDto {
    private Long folderId;
    private Long parentId;
    private String folderName;
    private Long channelId;
}
