package com.example.coconote.api.thread.tag.dto.response;

import com.example.coconote.api.search.dto.ThreadSearchResultDto;
import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagSearchListResDto {
    private Long threadId;
    private String content;
    private String memberNickName;
    private String profileImageUrl;
    private Long channelId;
    private String createdTime;
    private List<TagResDto> tags; // 태그 정보 추가
    private List<ThreadFileDto> fileUrls; // 파일 URL 정보 추가
    private Long parentThreadId; // 부모 쓰레드 ID 추가
}
