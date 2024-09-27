package com.example.coconote.api.search.mapper;


import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ThreadMapper {

    public ThreadDocument toDocument(Thread thread) {
        return ThreadDocument.builder()
                .threadId(String.valueOf(thread.getId()))
                .content(thread.getContent())
                .memberName(thread.getMember().getNickname())
                .channelId(thread.getChannel().getChannelId())
                .createdTime(thread.getCreatedTime().toString())
                .fileUrls(thread.getThreadFiles().stream()
                        .map(ThreadFile::getFileURL)  // 파일 URL 추출
                        .collect(Collectors.toList()))
                .build();
    }
}