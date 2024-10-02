package com.example.coconote.api.search.mapper;

import com.example.coconote.api.search.entity.FileEntityDocument;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import org.springframework.stereotype.Component;

@Component
public class FileEntityMapper {

    public FileEntityDocument toDocument(FileEntity fileEntity) {
        return FileEntityDocument.builder()
                .fileId(String.valueOf(fileEntity.getId()))
                .fileName(fileEntity.getFileName())
                .fileUrl(fileEntity.getFileUrl())
                .folderId(fileEntity.getFolder() != null ? fileEntity.getFolder().getId() : null)
                .creatorId(fileEntity.getCreator() != null ? fileEntity.getCreator().getId() : null)
                .isDeleted(fileEntity.getIsDeleted() == IsDeleted.Y)
                .deletedTime(fileEntity.getDeletedTime())
                .build();
    }
}