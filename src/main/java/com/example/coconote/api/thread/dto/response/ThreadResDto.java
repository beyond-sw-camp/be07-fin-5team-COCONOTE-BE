package com.example.coconote.api.thread.dto.response;

import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.entity.MessageType;
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
    private MessageType type;
    private String image;
    private String memberName;
    private String createdTime;
    private String content;
    private List<String> files;
    private List<ThreadResDto> childThreads;
    private List<TagResDto> tags;
}
