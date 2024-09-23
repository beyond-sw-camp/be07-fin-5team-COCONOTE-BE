package com.example.coconote.api.thread.dto.response;

import com.example.coconote.api.tag.dto.response.TagResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadResDto {
    private Long id;
    private String image;
    private String memberName;
    private String createdTime;
    private String content;
    private List<String> files;
    private List<ThreadResDto> childThreads;
    //TODO: 태그 추가해줘야됨
    private List<TagResDto> tags;
}
