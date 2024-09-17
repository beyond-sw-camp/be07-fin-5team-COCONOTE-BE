package com.example.coconote.global.fileUpload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {
    private String fileName;
    private long fileSize; // 용량 제한 검사를 위해 사용
}