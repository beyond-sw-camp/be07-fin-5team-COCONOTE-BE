package com.example.coconote.api.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDocument {
    @Id
    private String threadId;  // OpenSearch 문서 ID
    private String content;
    private String memberName;
    private Long channelId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdTime;
    private List<String> fileUrls;
    private List<String> tags; // 태그 정보 추가
    private List<ThreadDocument> childThreads; // 자식 쓰레드 정보 추가
    private Long parentThreadId; // 부모 쓰레드 ID 추가

}
