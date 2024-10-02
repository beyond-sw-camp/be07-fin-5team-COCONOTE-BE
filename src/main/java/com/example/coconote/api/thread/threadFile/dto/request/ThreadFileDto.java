package com.example.coconote.api.thread.threadFile.dto.request;

import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadFileDto {
    private Long fileId;
    private String fileURL;
    private String fileName;

    public ThreadFile toEntity(Thread thread) {
        return ThreadFile.builder()
                .fileID(this.fileId)
                .thread(thread)
                .fileURL(this.fileURL)
                .fileName(this.fileName)
                .build();
    }
}
