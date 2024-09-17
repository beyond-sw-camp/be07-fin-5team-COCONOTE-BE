package com.example.coconote.global.fileUpload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadataReqDto {
    private String fileName;
    private String fileUrl;
}
