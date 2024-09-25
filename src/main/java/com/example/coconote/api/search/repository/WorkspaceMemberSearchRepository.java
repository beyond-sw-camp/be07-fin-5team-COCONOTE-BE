package com.example.coconote.api.search.repository;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceMemberSearchRepository extends ElasticsearchRepository<WorkspaceMemberDocument, String>{
//    workspaceId 가 같은것 중 email, nickname 이 같은 리스트 조회
    @Query("{\"bool\": {\"must\": [{\"term\": {\"workspaceId\": \"?0\"}},{\"bool\": {\"should\": [{\"wildcard\": {\"email\": \"*?1*\"}},{\"wildcard\": {\"nickname\": \"*?1*\"}}]}}]}}")
    List<WorkspaceMemberDocument> findByWorkspaceIdAndEmailOrNickname(Long workspaceId, String keyword);
}