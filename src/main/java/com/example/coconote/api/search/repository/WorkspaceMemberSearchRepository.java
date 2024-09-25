package com.example.coconote.api.search.repository;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceMemberSearchRepository extends ElasticsearchRepository<WorkspaceMemberDocument, String> {
    // 이름, 이메일, 닉네임으로 검색
    List<WorkspaceMemberDocument> findByMemberNameContainingOrEmailContainingOrNicknameContaining(String memberName, String email, String nickname);
}