package com.example.coconote.api.drive.repository;

import com.example.coconote.api.drive.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {


    List<Folder> findAllByParentFolder(Folder folder);
}
