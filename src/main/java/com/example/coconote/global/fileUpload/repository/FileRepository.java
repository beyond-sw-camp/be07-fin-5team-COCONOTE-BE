package com.example.coconote.global.fileUpload.repository;


import com.example.coconote.global.fileUpload.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
