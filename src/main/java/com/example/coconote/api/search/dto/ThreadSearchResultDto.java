package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadSearchResultDto {
    private String threadId;
    private String content;
    private String memberName;
    private String profileImageUrl;
    private Long channelId;
    private String createdTime;
    private List<String> tags; // 태그 정보 추가
    private List<String> fileUrls; // 파일 URL 정보 추가
    private List<ThreadSearchResultDto> childThreads; // 자식 쓰레드 정보 추가
    private Long parentThreadId; // 부모 쓰레드 ID 추가

}