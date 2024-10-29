package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchResultDto {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long folderId;
    private Long channelId;
    private String channelName;
}