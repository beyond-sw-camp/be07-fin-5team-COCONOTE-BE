package com.example.coconote.global.fileUpload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSaveListDto {
    private String fileName;
    private String fileUrl;
}
