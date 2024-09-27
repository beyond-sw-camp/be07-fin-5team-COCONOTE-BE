package com.example.coconote.api.search.controller;

import com.example.coconote.api.search.entity.ChannelDocument;
import com.example.coconote.api.search.entity.FileEntityDocument;
import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    // 멤버 검색 API (이름, 이메일, 닉네임 검색)
    @GetMapping("/search/workspace/members")
    public ResponseEntity<List<WorkspaceMemberDocument>> searchWorkspaceMembers(@RequestParam Long workspaceId, @RequestParam String keyword) {
        List<WorkspaceMemberDocument> members = searchService.searchWorkspaceMembers(workspaceId, keyword);
        return ResponseEntity.ok(members);
    }

//    파일 검색 API (파일명, 파일 타입 검색)
    @GetMapping("/search/files")
    public ResponseEntity<List<FileEntityDocument>> searchFiles(@RequestParam Long workspaceId, @RequestParam String keyword) {
        List<FileEntityDocument> files = searchService.searchFiles(workspaceId, keyword);
        return ResponseEntity.ok(files);
    }

//    채널 검색 API (채널명, 채널 정보 검색)
    @GetMapping("/search/channels")
    public ResponseEntity<List<ChannelDocument>> searchChannels(@RequestParam Long workspaceId, @RequestParam String keyword) {
        List<ChannelDocument> channels = searchService.searchChannels(workspaceId, keyword);
        return ResponseEntity.ok(channels);
    }

//    쓰레드 검색 API
    @GetMapping("/search/threads")
    public ResponseEntity<List<ThreadDocument>> searchThreads(@RequestParam Long workspaceId, @RequestParam String keyword) {
        List<ThreadDocument> threads = searchService.searchThreads(workspaceId, keyword);
        return ResponseEntity.ok(threads);
    }

//    캔버스 블록 검색
    @GetMapping("/search/canvas/blocks")
    public ResponseEntity<?> searchCanvasBlocks(@RequestParam Long workspaceId, @RequestParam String keyword){
        return ResponseEntity.ok(searchService.searchCanvasAndBlocks(workspaceId, keyword));
    }
}
