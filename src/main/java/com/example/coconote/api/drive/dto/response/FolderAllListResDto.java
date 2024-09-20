package com.example.coconote.api.drive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderAllListResDto {
    private Long nowFolderId;
    private String nowFolderName;
    private List<FolderListDto> folderListDto;
    private List<FileListDto> fileListDto;
}
