package com.example.coconote.api.drive.repository;

import com.example.coconote.api.drive.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {


}
