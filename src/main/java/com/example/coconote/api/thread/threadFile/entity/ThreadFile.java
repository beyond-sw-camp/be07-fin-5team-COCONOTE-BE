package com.example.coconote.api.thread.threadFile.entity;

import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadFile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Thread thread;

    @Column(nullable = false, unique = true)
    private Long fileID;

    private String fileURL;

    private String fileName;

    public ThreadFileDto fromEntity() {
        return ThreadFileDto.builder()
                .fileId(this.fileID)
                .fileURL(this.fileURL)
                .fileName(this.fileName)
                .build();
    }

    public void markAsDeleted() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }
}
