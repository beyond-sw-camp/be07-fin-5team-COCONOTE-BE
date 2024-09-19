package com.example.coconote.global.fileUpload.repository;


import com.example.coconote.global.fileUpload.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByS3Key(String fileName);
}
