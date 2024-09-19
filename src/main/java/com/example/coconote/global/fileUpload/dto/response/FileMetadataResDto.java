package com.example.coconote.global.fileUpload.dto.response;

import com.example.coconote.global.fileUpload.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadataResDto {
    private Long id;
    private String fileName;
    private String fileUrl; // Presigned URL 저장


    public static FileMetadataResDto fromEntity(FileEntity fileEntity) {
        return FileMetadataResDto.builder()
                .id(fileEntity.getId())
                .fileName(fileEntity.getFileName())
                .fileUrl(fileEntity.getFileUrl())
                .build();
    }
}
