package com.example.coconote.api.thread.threadFile.entity;

import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Thread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    private FileEntity file;

    private String fileURL;

    private String fileName;
}
