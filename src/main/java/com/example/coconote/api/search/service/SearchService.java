package com.example.coconote.api.search.service;

import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchService {
    private final OpenSearchClient openSearchClient;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private static final String INDEX_NAME = "workspace_members";



    // OpenSearch 인덱스에 문서 저장
    public void indexWorkspaceMember(WorkspaceMember workspaceMember) {
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        try {
            openSearchClient.index(i -> i
                    .index(INDEX_NAME)  // 인덱스 이름
                    .id(document.getId())        // 문서 ID
                    .document(document)          // 문서 내용
            );
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch에 인덱싱 중 오류가 발생했습니다.", e);
        }
    }

    // OpenSearch 인덱스에서 문서 삭제
    public void deleteWorkspaceMember(String workspaceMemberId) {
        try {
            openSearchClient.delete(d -> d
                    .index(INDEX_NAME)  // 상수로 인덱스 이름 사용
                    .id(workspaceMemberId)
            );
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch에서 문서를 삭제하는 중 오류가 발생했습니다.", e);
        }
    }

    // OpenSearch query to search by email or nickname
    public List<WorkspaceMemberDocument> searchWorkspaceMembers(Long workspaceId, String keyword) {
        List<WorkspaceMemberDocument> results = new ArrayList<>();

        try {
            // Construct OpenSearch query
            SearchResponse<WorkspaceMemberDocument> searchResponse = openSearchClient.search(s -> s
                            .index("workspace_members")
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m.term(t -> t
                                                    .field("workspaceId")
                                                    .value(FieldValue.of(workspaceId))
                                            ))
                                            .should(sh -> sh.wildcard(w -> w
                                                    .field("email")
                                                    .value("*" + keyword + "*")
                                            ))
                                            .should(sh -> sh.wildcard(w -> w
                                                    .field("nickname")
                                                    .value("*" + keyword + "*")
                                            ))
                                            .minimumShouldMatch(String.valueOf(1))  // 최소 1개의 should 조건이 일치해야 함
                                    )
                            ),
                    WorkspaceMemberDocument.class
            );

            // Map the response hits to WorkspaceMemberDocument list
            searchResponse.hits().hits().forEach(hit -> results.add(hit.source()));

        } catch (IOException e) {
            // Handle exceptions
            throw new IllegalArgumentException("Failed to search workspace members");
        }

        return results;
    }
}

