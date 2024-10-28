package com.example.coconote.global.fileUpload.repository;


import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT fe FROM FileEntity fe WHERE fe.folder = :folder AND fe.isDeleted = :isDeleted")
    List<FileEntity> findAllFilesByFolderAndIsDeleted(Folder folder, IsDeleted isDeleted);

    void deleteAllByIsDeletedAndDeletedTimeBefore(IsDeleted isDeleted, LocalDateTime sevenDaysAgo);

    @Modifying
    @Transactional
    @Query("UPDATE FileEntity f SET f.isDeleted = :isDeleted, f.deletedTime = :now WHERE f.folder = :folder")
    void softDeleteFilesInFolder(IsDeleted isDeleted, LocalDateTime now, Folder folder);
}
