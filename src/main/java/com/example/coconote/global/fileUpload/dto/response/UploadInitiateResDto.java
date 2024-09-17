package com.example.coconote.global.fileUpload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UploadInitiateResDto {
    private String uploadId;
    private String objectName;
}

