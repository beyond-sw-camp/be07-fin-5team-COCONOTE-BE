package com.example.coconote.api.thread.thread.dto.response;

import com.example.coconote.api.thread.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<ThreadFileDto> files;
    private List<ThreadResDto> childThreads;
    private List<TagResDto> tags;
    private Long fileId;
    private Long tagId;
    private String tagName;
    private String tagColor;
}
