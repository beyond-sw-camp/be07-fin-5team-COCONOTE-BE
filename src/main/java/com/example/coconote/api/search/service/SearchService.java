package com.example.coconote.api.search.service;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.search.entity.*;
import com.example.coconote.api.search.mapper.*;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final OpenSearchClient openSearchClient;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final FileEntityMapper fileEntityMapper;
    private final ChannelMapper channelMapper;
    private final ThreadMapper threadMapper;
    private final CanvasBlockMapper canvasBlockMapper;

    // 워크스페이스 ID를 기반으로 에일리어스를 동적으로 생성
    private String getAliasForWorkspace(Long workspaceId) {
        return "workspace_" + workspaceId;
    }

    // 공통 인덱스 저장 메서드
    private <T> void indexDocument(String alias, String documentId, T document) {
        try {
            openSearchClient.index(i -> i.index(alias).id(documentId).document(document));
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch 인덱싱 중 오류가 발생했습니다.", e);
        }
    }

    // 공통 문서 삭제 메서드
    private void deleteDocument(String alias, String documentId) {
        try {
            openSearchClient.delete(d -> d.index(alias).id(documentId));
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch에서 문서를 삭제하는 중 오류가 발생했습니다.", e);
        }
    }

    // 여러 필드를 대상으로 한 공통 검색 메서드
    private <T> List<T> searchDocumentsForMultipleFields(String alias, String keyword, List<String> fields, Class<T> documentClass, int from, int size) {
        List<T> results = new ArrayList<>();
        try {
            SearchResponse<T> searchResponse = openSearchClient.search(s -> s
                            .index(alias)
                            .from(from)
                            .size(size)
                            .query(q -> q
                                    .bool(b -> {
                                        fields.forEach(field -> b.should(sh -> sh.wildcard(w -> w.field(field).value("*" + keyword + "*"))));
                                        return b.minimumShouldMatch(String.valueOf(1));
                                    })
                            ),
                    documentClass
            );
            searchResponse.hits().hits().forEach(hit -> results.add(hit.source()));
        } catch (IOException e) {
            throw new IllegalArgumentException("OpenSearch 검색 중 오류가 발생했습니다.", e);
        }
        return results;
    }

    // 자동완성 기능 추가 (여러 필드를 동시에 검색)
// 자동완성 기능 수정
    public List<String> getAutocompleteSuggestions(Long workspaceId, String keyword, List<String> fields) {
        List<String> suggestions = new ArrayList<>();
        String alias = getAliasForWorkspace(workspaceId);

        try {
            // OpenSearch에서 자동완성 검색을 수행
            SearchResponse<Map> searchResponse = openSearchClient.search(s -> s
                            .index(alias)
                            .query(q -> q
                                    .bool(b -> {
                                        fields.forEach(field -> b.should(sh -> sh.wildcard(w -> w
                                                .field(field)
                                                .value("*" + keyword + "*"))));
                                        return b.minimumShouldMatch("1");  // 최소 1개의 조건을 만족하는 결과 반환
                                    })
                            )
                            .size(10),  // 최대 10개의 제안어 반환
                    Map.class // 검색 결과를 Map<String, Object>로 처리
            );

            // 검색 결과에서 각 필드에 해당하는 값 추출
            searchResponse.hits().hits().forEach(hit -> {
                fields.forEach(field -> {
                    Object fieldValue = hit.source().get(field);
                    if (fieldValue instanceof String && !suggestions.contains(fieldValue)) {  // 중복 제거
                        suggestions.add((String) fieldValue);
                    }
                });
            });

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to fetch autocomplete suggestions", e);
        }

        return suggestions;
    }
    // 워크스페이스 멤버 검색
    public List<WorkspaceMemberDocument> searchWorkspaceMembers(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocumentsForMultipleFields(alias, keyword, List.of("email", "nickname"), WorkspaceMemberDocument.class, page * size, size);
    }

    // 파일 검색
    public List<FileEntityDocument> searchFiles(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocumentsForMultipleFields(alias, keyword, List.of("fileName"), FileEntityDocument.class, page * size, size);
    }

    // 채널 검색
    public List<ChannelDocument> searchChannels(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocumentsForMultipleFields(alias, keyword, List.of("channelName"), ChannelDocument.class, page * size, size);
    }

    // 쓰레드 검색
    public List<ThreadDocument> searchThreads(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocumentsForMultipleFields(alias, keyword, List.of("title", "content"), ThreadDocument.class, page * size, size);
    }

    // 캔버스 & 블록 검색
    public List<CanvasBlockDocument> searchCanvasAndBlocks(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        return searchDocumentsForMultipleFields(alias, keyword, List.of("canvasTitle", "blockContents"), CanvasBlockDocument.class, page * size, size);
    }

    // 전체 검색 (모든 인덱스에서 검색)
    public List<Object> searchAll(Long workspaceId, String keyword, int page, int size) {
        List<Object> results = new ArrayList<>();
        String alias = getAliasForWorkspace(workspaceId);
        int from = page * size;

        results.addAll(searchWorkspaceMembers(workspaceId, keyword, from, size));
        results.addAll(searchFiles(workspaceId, keyword, from, size));
        results.addAll(searchChannels(workspaceId, keyword, from, size));
        results.addAll(searchThreads(workspaceId, keyword, from, size));
        results.addAll(searchCanvasAndBlocks(workspaceId, keyword, from, size));

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

    // 쓰레드 인덱스 저장
    public void indexThread(Long workspaceId, Thread thread) {
        ThreadDocument document = threadMapper.toDocument(thread);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, document.getThreadId(), document);
    }

    // 쓰레드 삭제
    public void deleteThread(Long workspaceId, String threadId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, threadId);
    }

    // 캔버스 인덱스 저장
    public void indexCanvas(Long workspaceId, Canvas canvas) {
        CanvasBlockDocument canvasBlockDocument = canvasBlockMapper.toDocument(canvas);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, canvasBlockDocument.getCanvasId(), canvasBlockDocument);
    }

    // 블록 인덱스 저장
    public void indexBlock(Long workspaceId, Block block) {
        CanvasBlockDocument canvasBlockDocument = canvasBlockMapper.toDocument(block);
        String alias = getAliasForWorkspace(workspaceId);
        indexDocument(alias, canvasBlockDocument.getId(), canvasBlockDocument);
    }

    // 캔버스 삭제
    public void deleteCanvas(Long workspaceId, Long canvasId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, String.valueOf(canvasId));
    }

    // 블록 삭제
    public void deleteBlock(Long workspaceId, String blockId) {
        String alias = getAliasForWorkspace(workspaceId);
        deleteDocument(alias, blockId);
    }
}
