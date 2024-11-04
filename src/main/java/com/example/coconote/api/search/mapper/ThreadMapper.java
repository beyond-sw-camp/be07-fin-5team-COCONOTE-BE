package com.example.coconote.api.search.mapper;


import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import com.example.coconote.common.IsDeleted;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ThreadMapper {

    public ThreadDocument toDocument(Thread thread) {
        // 파일 목록 변환
        List<String> fileUrls = thread.getThreadFiles().stream()
                .map(ThreadFile::getFileURL)
                .collect(Collectors.toList());

        // 태그 목록 변환 - 삭제되지 않은 태그만 포함
        List<String> tags = thread.getThreadTags().stream()
                .map(ThreadTag::getTag)
                .filter(tag -> tag.getIsDeleted() == IsDeleted.N) // 삭제되지 않은 태그만 포함
                .map(Tag::getName)
                .collect(Collectors.toList());;

        // 자식 쓰레드 변환
        List<ThreadDocument> childThreads = thread.getChildThreads().stream()
                .map(childThread -> toDocument(childThread))
                .collect(Collectors.toList());
        // 부모 쓰레드 ID 가져오기
        Long parentThreadId = thread.getParent() != null ? thread.getParent().getId() : null;

        // ThreadDocument 빌드
        return ThreadDocument.builder()
                .threadId(String.valueOf(thread.getId()))
                .content(thread.getContent())
                .workspaceMemberId(thread.getWorkspaceMember().getWorkspaceMemberId())
                .channelId(thread.getChannel().getChannelId())
                .createdTime(thread.getCreatedTime().toString())
                .fileUrls(fileUrls)
                .tags(tags)
                .childThreads(childThreads) // 자식 쓰레드 설정
                .parentThreadId(parentThreadId) // 부모 쓰레드 ID 설정
                .build();
    }
}