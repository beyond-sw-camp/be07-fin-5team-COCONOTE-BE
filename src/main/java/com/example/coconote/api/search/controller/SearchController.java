package com.example.coconote.api.search.controller;

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
}
