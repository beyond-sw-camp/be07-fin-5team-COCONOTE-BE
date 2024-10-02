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
public class CombinedSearchResultDto {
    private List<WorkspaceMemberSearchResultDto> workspaceMembers;
    private List<FileSearchResultDto> files;
    private List<ChannelSearchResultDto> channels;
    private List<ThreadSearchResultDto> threads;
    private List<CanvasBlockSearchResultDto> canvasBlocks;

    // 각 카테고리별 검색 결과의 총 개수
    private long totalMembers;
    private long totalFiles;
    private long totalChannels;
    private long totalThreads;
    private long totalCanvasBlocks;
}

