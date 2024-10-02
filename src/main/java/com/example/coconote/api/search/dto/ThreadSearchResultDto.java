package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadSearchResultDto {
    private String threadId;
    private String content;
    private String memberName;
    private Long channelId;
    private String createdTime;
}