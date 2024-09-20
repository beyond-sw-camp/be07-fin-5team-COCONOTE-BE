package com.example.coconote.global.fileUpload.repository;


import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findAllByFolderAndIsDeleted(Folder folder, IsDeleted isDeleted);
}
