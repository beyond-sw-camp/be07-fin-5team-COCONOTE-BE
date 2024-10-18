package com.example.coconote.api.search.controller;

import com.example.coconote.api.search.dto.*;
import com.example.coconote.api.search.entity.*;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    // 통합 검색 API
    @GetMapping("/search")
    public ResponseEntity<?> searchAll(
            @RequestParam Long workspaceId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        CombinedSearchResultDto searchResult = searchService.searchAll(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", searchResult));
    }


    // 자동완성 API
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam Long workspaceId, @RequestParam String keyword, @RequestParam SearchTarget target) {
        List<String> fields = getFieldsBySearchTarget(target);  // SearchTarget에 따른 필드 목록
        List<String> suggestions = searchService.getAutocompleteSuggestions(workspaceId, keyword, fields);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Autocomplete Successful", suggestions));
    }

    // SearchTarget에 따라 자동완성 필드를 선택하는 메서드
    private List<String> getFieldsBySearchTarget(SearchTarget target) {
        return switch (target) {
            case MEMBER -> List.of("memberName" );  // 멤버는 nickname과 email 필드 둘 다 검색
            case FILE -> List.of("fileName");  // 파일은 파일명 필드
            case CHANNEL -> List.of("channelName");  // 채널은 채널명 필드
            case THREAD -> List.of("title", "content");  // 쓰레드는 제목과 내용 필드 둘 다 검색
            case CANVAS_BLOCK -> List.of("canvasTitle", "blockContents");  // 캔버스 & 블록은 제목과 블록 내용 둘 다 검색
            default -> List.of("memberName","email","channelName", "title","content", "canvasTitle", "blockContents");  // 기본값으로 멤버의 닉네임
        };
    }

    // 멤버 검색 API (이름, 이메일, 닉네임 검색)
    @GetMapping("/search/members")
    public ResponseEntity<?> searchWorkspaceMembers(@RequestParam Long workspaceId,
                                                                                @RequestParam String keyword,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size) {
        SearchResultWithTotal<?> memberResults = searchService.searchWorkspaceMembers(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", memberResults));
    }

//    파일 검색 API (파일명, 파일 타입 검색)
    @GetMapping("/search/files")
    public ResponseEntity<?> searchFiles(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        SearchResultWithTotal<?> fileResults = searchService.searchFiles(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", fileResults));
    }

//    채널 검색 API (채널명, 채널 정보 검색)
    @GetMapping("/search/channels")
    public ResponseEntity<?> searchChannels(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        SearchResultWithTotal<?> channelResults = searchService.searchChannels(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", channelResults));
    }

//    쓰레드 검색 API
    @GetMapping("/search/threads")
    public ResponseEntity<?> searchThreads(@RequestParam Long workspaceId, @RequestParam String keyword,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        SearchResultWithTotal<?> threadResults = searchService.searchThreads(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", threadResults));
    }

//    캔버스 블록 검색
    @GetMapping("/search/canvasblocks")
    public ResponseEntity<?> searchCanvasBlocks(@RequestParam Long workspaceId, @RequestParam String keyword,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        SearchResultWithTotal<?> canvasBlockResults = searchService.searchCanvasAndBlocks(workspaceId, keyword, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", canvasBlockResults));
    }

    // 태그를 통한 쓰레드 검색 API
    @GetMapping("/search/threads/by-tags")
    public ResponseEntity<?> searchThreadsByTags(
            @RequestParam Long workspaceId,
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomPrincipal member) {
        SearchResultWithTotal<ThreadSearchResultDto> threadResults = searchService.searchThreadsByTags(workspaceId, tags, page, size);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "Search Successful", threadResults));
    }

}
