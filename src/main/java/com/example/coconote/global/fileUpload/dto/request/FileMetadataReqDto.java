package com.example.coconote.global.fileUpload.dto.request;

import com.example.coconote.global.fileUpload.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadataReqDto {
    private Long channelId;
    private Long folderId;
    private List<FileSaveListDto> fileSaveListDto;
    private FileType fileType;  // Enum 필드 추가
}
