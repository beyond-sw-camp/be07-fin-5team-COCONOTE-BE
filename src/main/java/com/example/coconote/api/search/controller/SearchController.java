package com.example.coconote.api.search.controller;

import com.example.coconote.api.search.entity.*;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.common.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    // 통합 검색 API
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Long workspaceId,
                                    @RequestParam String keyword,
                                    @RequestParam(defaultValue = "all") SearchTarget target,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        // target에 따라 전체 검색 또는 특정 인덱스 검색
        if (target == SearchTarget.ALL) {
            List<Object> searchAll = searchService.searchAll(workspaceId, keyword, page, size);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", searchAll));
        } else {
            return switch (target) {
                case MEMBER -> ResponseEntity.ok(
                        new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchWorkspaceMembers(workspaceId, keyword, page, size))
                );
                case FILE -> ResponseEntity.ok(
                        new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchFiles(workspaceId, keyword, page, size))
                );
                case CHANNEL -> ResponseEntity.ok(
                        new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchChannels(workspaceId, keyword, page, size))
                );
                case THREAD -> ResponseEntity.ok(
                        new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchThreads(workspaceId, keyword, page, size))
                );
                case CANVAS_BLOCK -> ResponseEntity.ok(
                        new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchCanvasAndBlocks(workspaceId, keyword, page, size))
                );
                default -> ResponseEntity.badRequest().body(
                        new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid search target.", null)
                );
            };
        }
    }

    // 멤버 검색 API (이름, 이메일, 닉네임 검색)
    @GetMapping("/search/workspace/members")
    public ResponseEntity<?> searchWorkspaceMembers(@RequestParam Long workspaceId,
                                                                                @RequestParam String keyword,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size) {
        List<WorkspaceMemberDocument> members = searchService.searchWorkspaceMembers(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", members));
    }

//    파일 검색 API (파일명, 파일 타입 검색)
    @GetMapping("/search/files")
    public ResponseEntity<?> searchFiles(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        List<FileEntityDocument> files = searchService.searchFiles(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", files));
    }

//    채널 검색 API (채널명, 채널 정보 검색)
    @GetMapping("/search/channels")
    public ResponseEntity<?> searchChannels(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        List<ChannelDocument> channels = searchService.searchChannels(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", channels));
    }

//    쓰레드 검색 API
    @GetMapping("/search/threads")
    public ResponseEntity<?> searchThreads(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        List<ThreadDocument> threads = searchService.searchThreads(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", threads));
    }

//    캔버스 블록 검색
    @GetMapping("/search/canvas/blocks")
    public ResponseEntity<?> searchCanvasBlocks(@RequestParam Long workspaceId, @RequestParam String keyword,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", searchService.searchCanvasAndBlocks(workspaceId, keyword, page, size)));
    }
}
