package com.example.coconote.global.fileUpload.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PresignedUrlResDto {
    private String url;
    private String key;
    private String fileUrl;
}
