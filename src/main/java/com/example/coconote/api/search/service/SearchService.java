package com.example.coconote.api.search.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.search.entity.ChannelDocument;
import com.example.coconote.api.search.entity.FileEntityDocument;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.ChannelMapper;
import com.example.coconote.api.search.mapper.FileEntityMapper;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
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
    private final FileEntityMapper fileEntityMapper;
    private final ChannelMapper channelMapper;


    // 워크스페이스 ID를 기반으로 에일리어스를 동적으로 생성
    private String getAliasForWorkspace(Long workspaceId) {
        return "workspace_" + workspaceId;  // 에일리어스 이름 생성
    }

    // 공통 인덱스 저장 메서드
    private <T> void indexDocument(String alias, String documentId, T document) {
        try {
            openSearchClient.index(i -> i
                    .index(alias)
                    .id(documentId)
                    .document(document)
            );
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch 인덱싱 중 오류가 발생했습니다.", e);
        }
    }

    // 공통 문서 삭제 메서드
    private void deleteDocument(String alias, String documentId) {
        try {
            openSearchClient.delete(d -> d
                    .index(alias)
                    .id(documentId)
            );
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch에서 문서를 삭제하는 중 오류가 발생했습니다.", e);
        }
    }

    // 공통 검색 메서드
    private <T> List<T> searchDocuments(String alias, String field1, String value1, String field2, String value2, Class<T> documentClass) {
        List<T> results = new ArrayList<>();
        try {
            SearchResponse<T> searchResponse = openSearchClient.search(s -> s
                            .index(alias)
                            .query(q -> q
                                    .bool(b -> b
                                            .should(sh -> sh.wildcard(w -> w
                                                    .field(field1)
                                                    .value("*" + value1 + "*")
                                            ))
                                            .should(sh -> sh.wildcard(w -> w
                                                    .field(field2)
                                                    .value("*" + value2 + "*")
                                            ))
                                            .minimumShouldMatch(String.valueOf(1))
                                    )
                            ),
                    documentClass
            );

            searchResponse.hits().hits().forEach(hit -> results.add(hit.source()));

        } catch (IOException e) {
            throw new IllegalArgumentException("OpenSearch 검색 중 오류가 발생했습니다.", e);
        }
        return results;
    }

    // 워크스페이스 멤버 인덱스 저장
    public void indexWorkspaceMember(Long workspaceId, WorkspaceMember workspaceMember) {
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, document.getId(), document);
    }

    // 워크스페이스 멤버 삭제
    public void deleteWorkspaceMember(Long workspaceId, String workspaceMemberId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, workspaceMemberId);
    }

    // 워크스페이스 멤버 검색
    public List<WorkspaceMemberDocument> searchWorkspaceMembers(Long workspaceId, String keyword) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocuments(alias, "email", keyword, "nickname", keyword, WorkspaceMemberDocument.class);
    }

    // 파일 인덱스 저장
    public void indexFileEntity(Long workspaceId, FileEntity fileEntity) {
        FileEntityDocument document = fileEntityMapper.toDocument(fileEntity);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, document.getFileId(), document);
    }

    // 파일 삭제
    public void deleteFileEntity(Long workspaceId, String fileId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, fileId);
    }

    // 파일 검색
    public List<FileEntityDocument> searchFiles(Long workspaceId, String keyword) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocuments(alias, "fileName", keyword, "fileUrl", keyword, FileEntityDocument.class);
    }

    // 채널 인덱스 저장
    public void indexChannel(Long workspaceId, Channel channel) {
        ChannelDocument document = channelMapper.toDocument(channel);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, document.getChannelId(), document);
    }

    // 채널 삭제
    public void deleteChannel(Long workspaceId, String channelId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, channelId);
    }

    // 채널 검색
    public List<ChannelDocument> searchChannels(Long workspaceId, String keyword) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocuments(alias, "channelName", keyword, "channelInfo", keyword, ChannelDocument.class);
    }
}
