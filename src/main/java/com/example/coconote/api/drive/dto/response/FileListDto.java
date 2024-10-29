package com.example.coconote.api.drive.dto.response;

import com.example.coconote.global.fileUpload.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileListDto {
    private Long fileId;
    private String fileName;
    private String fileUrl;
    private String creator;
    private String createdDate;

    public static List<FileListDto> fromEntity(List<FileEntity> fileEntityList) {
        return fileEntityList.stream()
                .map(fileEntity -> FileListDto.builder()
                        .fileId(fileEntity.getId())
                        .fileName(fileEntity.getFileName())
                        .fileUrl(fileEntity.getFileUrl())
                        .creator(fileEntity.getCreator().getNickname())
                        .createdDate(fileEntity.getCreatedTime().toString())
                        .build())
                .toList();
    }
}
