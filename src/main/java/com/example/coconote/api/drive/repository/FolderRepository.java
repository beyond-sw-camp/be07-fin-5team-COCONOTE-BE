package com.example.coconote.api.drive.repository;

import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByParentFolderAndIsDeleted(Folder folder, IsDeleted isDeleted);

    @Modifying // update, delete 쿼리를 실행하기 위한 어노테이션
    @Transactional
    @Query("UPDATE Folder f SET f.isDeleted = :deleted, f.deletedTime = :deletedTime WHERE f.parentFolder = :parentFolder")
    void softDeleteChildFolders(IsDeleted deleted, LocalDateTime deletedTime, Folder parentFolder);
}
