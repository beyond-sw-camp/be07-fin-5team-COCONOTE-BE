package com.example.coconote.api.search.service;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.repository.WorkspaceMemberSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final WorkspaceMemberSearchRepository workspaceMemberSearchRepository;

    // 멤버 이름, 이메일, 닉네임을 기반으로 검색
    public List<WorkspaceMemberDocument> searchWorkspaceMembers(String keyword) {
        return workspaceMemberSearchRepository.findByMemberNameContainingOrEmailContainingOrNicknameContaining(keyword, keyword, keyword);
    }
}
