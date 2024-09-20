package com.example.coconote.api.drive.repository;

import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByParentFolderAndIsDeleted(Folder folder, IsDeleted isDeleted);
}
