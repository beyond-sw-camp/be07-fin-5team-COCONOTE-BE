package com.example.coconote.global.fileUpload.entity;

import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member creator;

    public void moveFolder(Folder folder) {
        this.folder = folder;
    }

    public void markAsDeleted() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }

    public void renameFile(String newFileName) {
        this.fileName = newFileName;
    }
}
