package com.example.coconote.api.search.service;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.search.dto.*;
import com.example.coconote.api.search.entity.*;
import com.example.coconote.api.search.mapper.*;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // 고유한 documentId 생성 메서드
    private String generateDocumentId(String prefix, Long id) {
        return prefix + "_" + id;
    }

    // 통합 Kafka Listener: 모든 인덱싱 메시지를 처리
    @KafkaListener(topics = "thread_entity_search", groupId = "search-group")
    public void consumeIndexEntityMessage(String message) {
        log.info("Received Kafka message: {}", message);

        try {
            // ObjectMapper 설정
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())  // Java 8 날짜/시간 지원
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // 문자열 안에 있는 이스케이프된 JSON 형식을 해제
            String unescapedMessage = objectMapper.readValue(message, String.class);

            // 해제된 JSON을 JsonNode로 파싱
            JsonNode jsonNode = objectMapper.readTree(unescapedMessage);
            log.info("Deserialized JsonNode: {}", jsonNode);

            // workspaceId 추출
            long workspaceId = jsonNode.path("workspaceId").asLong();
            log.info("Deserialized workspaceId: {}", workspaceId);

            // entity 부분만 ThreadDocument로 역직렬화
            ThreadDocument threadDocument = objectMapper.treeToValue(jsonNode.get("entity"), ThreadDocument.class);
            log.info("Deserialized ThreadDocument: {}", threadDocument);

            // 쓰레드 인덱싱
            indexThread(workspaceId, threadDocument);
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
            throw new RuntimeException("Error processing Kafka message", e);
        }
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
    private <T> SearchResponse<T> searchDocumentsForMultipleFields(String alias, String keyword, List<String> fields, Class<T> documentClass, int page, int size) {
        try {
            // OpenSearch에서 검색 수행
            SearchResponse<T> searchResponse = openSearchClient.search(s -> s
                            .index(alias)
                            .from(page * size) // 페이징 처리 (시작 위치)
                            .size(size) // 한 페이지에 반환할 결과 개수
                            .query(q -> q
                                    .bool(b -> {
                                        fields.forEach(field -> {
                                            if (field.equals("email")) {
                                                // 이메일 필드에 match 쿼리 사용
                                                b.should(sh -> sh.matchPhrasePrefix(m -> m.field(field).query(keyword)));
                                            } else {
                                                // 다른 필드는 여전히 wildcard 검색 사용
                                                b.should(sh -> sh.wildcard(w -> w.field(field).value("*" + keyword + "*")));
                                            }
                                        });
                                        return b.minimumShouldMatch("1");
                                    })
                            ),
                    documentClass
            );
            return searchResponse; // 검색 결과와 총 개수 반환
        } catch (IOException e) {
            throw new IllegalArgumentException("OpenSearch 검색 중 오류가 발생했습니다.", e);
        }
    }


    // 자동완성 기능 추가 (여러 필드를 동시에 검색)
// 자동완성 기능 수정
    public List<String> getAutocompleteSuggestions(Long workspaceId, String keyword, List<String> fields) {
        List<String> suggestions = new ArrayList<>();
        String alias = getAliasForWorkspace(workspaceId);

        try {
            SearchResponse<Map> searchResponse = openSearchClient.search(s -> s
                            .index(alias)
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields(fields)
                                            .query(keyword)
                                            .type(TextQueryType.PhrasePrefix)  // PHRASE_PREFIX로 프리픽스 검색
                                    )
                            )
                            .size(10),
                    Map.class
            );

            searchResponse.hits().hits().forEach(hit -> {
                fields.forEach(field -> {
                    Object fieldValue = hit.source().get(field);
                    if (fieldValue instanceof String && !suggestions.contains(fieldValue)) {
                        suggestions.add((String) fieldValue);
                    }
                });
            });

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to fetch autocomplete suggestions", e);
        }

        return suggestions;
    }

    private String escapeSpecialChars(String keyword) {
        return keyword.replaceAll("([+\\-!(){}\\[\\]^\"~*?:\\\\/@])", "\\\\$1");
    }

    // 워크스페이스 멤버 검색 (총 결과 수 포함)
    public SearchResultWithTotal<WorkspaceMemberSearchResultDto> searchWorkspaceMembers(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);

        // 검색어에 특수문자 이스케이프 처리 추가
//        String escapedKeyword = escapeSpecialChars(keyword);
//
        // OpenSearch로 검색
        SearchResponse<WorkspaceMemberDocument> response = searchDocumentsForMultipleFields(alias, keyword, List.of("email", "nickname"), WorkspaceMemberDocument.class, page, size);

        // DTO로 변환
        List<WorkspaceMemberSearchResultDto> workspaceMembers = response.hits().hits().stream()
                .map(document -> WorkspaceMemberSearchResultDto.fromDocument(document.source()))
                .collect(Collectors.toList());

        // 총 검색 결과 수와 함께 반환
        return new SearchResultWithTotal<>(workspaceMembers, response.hits().total().value());
    }


    // 파일 검색
//    public List<FileSearchResultDto> searchFiles(Long workspaceId, String keyword, int page, int size) {
//        String alias = getAliasForWorkspace(workspaceId);
//        List<FileEntityDocument> documents = searchDocumentsForMultipleFields(alias, keyword, List.of("fileName"), FileEntityDocument.class, page * size, size);
//
//        return documents.stream().map(document -> FileSearchResultDto.builder()
//                        .fileId(document.getFileId())
//                        .fileName(document.getFileName())
//                        .fileUrl(document.getFileUrl())
//                        .folderId(document.getFolderId())
//                        .build())
//                .collect(Collectors.toList());
//    }
    // 파일 검색 (총 결과 수 포함)
    public SearchResultWithTotal<FileSearchResultDto> searchFiles(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        SearchResponse<FileEntityDocument> response = searchDocumentsForMultipleFields(alias, keyword, List.of("fileName"), FileEntityDocument.class, page, size);

        // DTO로 변환
        List<FileSearchResultDto> files = response.hits().hits().stream()
                .map(document -> FileSearchResultDto.builder()
                        .fileId(document.source().getFileId())
                        .fileName(document.source().getFileName())
                        .fileUrl(document.source().getFileUrl())
                        .folderId(document.source().getFolderId())
                        .build())
                .collect(Collectors.toList());

        return new SearchResultWithTotal<>(files, response.hits().total().value());
    }

    // 채널 검색
//    public List<ChannelSearchResultDto> searchChannels(Long workspaceId, String keyword, int page, int size) {
//        String alias = getAliasForWorkspace(workspaceId);
//        List<ChannelDocument> documents = searchDocumentsForMultipleFields(alias, keyword, List.of("channelName"), ChannelDocument.class, page * size, size);
//
//        return documents.stream().map(document -> ChannelSearchResultDto.builder()
//                        .channelId(document.getChannelId())
//                        .channelName(document.getChannelName())
//                        .channelInfo(document.getChannelInfo())
//                        .isPublic(document.getIsPublic())
//                        .build())
//                .collect(Collectors.toList());
//    }

    // 채널 검색 (총 결과 수 포함)
    public SearchResultWithTotal<ChannelSearchResultDto> searchChannels(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        SearchResponse<ChannelDocument> response = searchDocumentsForMultipleFields(alias, keyword, List.of("channelName"), ChannelDocument.class, page, size);

        // DTO로 변환
        List<ChannelSearchResultDto> channels = response.hits().hits().stream()
                .map(document -> ChannelSearchResultDto.builder()
                        .channelId(document.source().getChannelId())
                        .channelName(document.source().getChannelName())
                        .channelInfo(document.source().getChannelInfo())
                        .isPublic(document.source().getIsPublic())
                        .build())
                .collect(Collectors.toList());

        return new SearchResultWithTotal<>(channels, response.hits().total().value());
    }

    // 쓰레드 검색
//    public List<ThreadSearchResultDto> searchThreads(Long workspaceId, String keyword, int page, int size) {
//        String alias = getAliasForWorkspace(workspaceId);
//        List<ThreadDocument> documents = searchDocumentsForMultipleFields(alias, keyword, List.of("title", "content"), ThreadDocument.class, page * size, size);
//
//        return documents.stream().map(document -> ThreadSearchResultDto.builder()
//                        .threadId(document.getThreadId())
//                        .content(document.getContent())
//                        .memberName(document.getMemberName())
//                        .channelId(document.getChannelId())
//                        .createdTime(document.getCreatedTime())
//                        .build())
//                .collect(Collectors.toList());
//    }

    // 쓰레드 검색 (총 결과 수 포함)
    public SearchResultWithTotal<ThreadSearchResultDto> searchThreads(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        SearchResponse<ThreadDocument> response = searchDocumentsForMultipleFields(alias, keyword, List.of("title", "content"), ThreadDocument.class, page, size);

        // DTO로 변환
        List<ThreadSearchResultDto> threads = response.hits().hits().stream()
                .map(document -> ThreadSearchResultDto.builder()
                        .threadId(document.source().getThreadId())
                        .content(document.source().getContent())
                        .memberName(document.source().getMemberName())
                        .channelId(document.source().getChannelId())
                        .createdTime(document.source().getCreatedTime())
                        .build())
                .collect(Collectors.toList());

        return new SearchResultWithTotal<>(threads, response.hits().total().value());
    }

    // 캔버스 & 블록 검색
//    public List<CanvasBlockSearchResultDto> searchCanvasAndBlocks(Long workspaceId, String keyword, int page, int size) {
//        String alias = getAliasForWorkspace(workspaceId);
//        List<CanvasBlockDocument> documents = searchDocumentsForMultipleFields(alias, keyword, List.of("canvasTitle", "blockContents"), CanvasBlockDocument.class, page * size, size);
//
//        return documents.stream().map(document -> CanvasBlockSearchResultDto.builder()
//                        .id(document.getId())
//                        .canvasTitle(document.getCanvasTitle())
//                        .blockContents(document.getBlockContents())
//                        .build())
//                .collect(Collectors.toList());
//    }

    // 캔버스 & 블록 검색 (총 결과 수 포함)
    public SearchResultWithTotal<CanvasBlockSearchResultDto> searchCanvasAndBlocks(Long workspaceId, String keyword, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);
        SearchResponse<CanvasBlockDocument> response = searchDocumentsForMultipleFields(alias, keyword, List.of("canvasTitle", "blockContents"), CanvasBlockDocument.class, page, size);

        // DTO로 변환
        List<CanvasBlockSearchResultDto> canvasBlocks = response.hits().hits().stream()
                .map(document -> CanvasBlockSearchResultDto.builder()
                        .id(document.source().getId())
                        .canvasTitle(document.source().getCanvasTitle())
                        .blockContents(document.source().getBlockContents())
                        .build())
                .collect(Collectors.toList());

        return new SearchResultWithTotal<>(canvasBlocks, response.hits().total().value());
    }

    // 전체 검색 (모든 인덱스에서 검색)
    public CombinedSearchResultDto searchAll(Long workspaceId, String keyword, int page, int size) {
        SearchResultWithTotal<WorkspaceMemberSearchResultDto> memberResult = searchWorkspaceMembers(workspaceId, keyword, page, size);
        SearchResultWithTotal<FileSearchResultDto> fileResult = searchFiles(workspaceId, keyword, page, size);
        SearchResultWithTotal<ChannelSearchResultDto> channelResult = searchChannels(workspaceId, keyword, page, size);
        SearchResultWithTotal<ThreadSearchResultDto> threadResult = searchThreads(workspaceId, keyword, page, size);
        SearchResultWithTotal<CanvasBlockSearchResultDto> canvasBlockResult = searchCanvasAndBlocks(workspaceId, keyword, page, size);


        return CombinedSearchResultDto.builder()
                .workspaceMembers(memberResult.getResults())
                .files(fileResult.getResults())
                .channels(channelResult.getResults())
                .threads(threadResult.getResults())
                .canvasBlocks(canvasBlockResult.getResults())
                .totalMembers(memberResult.getTotal())
                .totalFiles(fileResult.getTotal())
                .totalChannels(channelResult.getTotal())
                .totalThreads(threadResult.getTotal())
                .totalCanvasBlocks(canvasBlockResult.getTotal())
                .build();
    }

    // 워크스페이스 멤버 인덱스 저장
    @Async
    public CompletableFuture<Void> indexWorkspaceMember(Long workspaceId, WorkspaceMember workspaceMember) {
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("workspaceMember", workspaceMember.getWorkspaceMemberId());
        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);
        });
    }

    // 워크스페이스 멤버 삭제
    public void deleteWorkspaceMember(Long workspaceId, Long workspaceMemberId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("workspaceMember", workspaceMemberId);
        deleteDocument(alias, documentId);
    }

    // 파일 인덱스 저장
    @Async
    public CompletableFuture<Void> indexFileEntity(Long workspaceId, FileEntity fileEntity) {
        FileEntityDocument document = fileEntityMapper.toDocument(fileEntity);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("fileEntity", fileEntity.getId());
        indexDocument(alias, documentId, document);

        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);
        });
    }

    // 파일 삭제
    public void deleteFileEntity(Long workspaceId, Long fileId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("fileEntity", fileId);
        deleteDocument(alias, documentId);
    }

    // 채널 인덱스 저장
    @Async
    public CompletableFuture<Void> indexChannel(Long workspaceId, Channel channel) {
        ChannelDocument document = channelMapper.toDocument(channel);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("channel", channel.getChannelId());
        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);
        });
    }

    // 채널 삭제
    public void deleteChannel(Long workspaceId, Long channelId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("channel", channelId);
        deleteDocument(alias, documentId);
    }

    // 쓰레드 인덱스 저장
// 쓰레드 인덱스 저장
    @Async
    public CompletableFuture<Void> indexThread(Long workspaceId, ThreadDocument document) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("thread", Long.valueOf(document.getThreadId()));  // threadId를 Long으로 변환
        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);
        });
    }

    // 쓰레드 삭제
    public void deleteThread(Long workspaceId, Long threadId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("thread", threadId);
        deleteDocument(alias, documentId);
    }

    // 캔버스 인덱스 저장
    @Async
    public CompletableFuture<Void> indexCanvas(Long workspaceId, Canvas canvas) {
        CanvasBlockDocument canvasBlockDocument = canvasBlockMapper.toDocument(canvas);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("canvas", canvas.getId());
        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, canvasBlockDocument);
        });
    }

    // 캔버스 삭제
    public void deleteCanvas(Long workspaceId, Long canvasId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("canvas", canvasId);
        deleteDocument(alias, documentId);
    }

    // 블록 인덱스 저장
    @Async
    public CompletableFuture<Void> indexBlock(Long workspaceId, Block block) {
        CanvasBlockDocument canvasBlockDocument = canvasBlockMapper.toDocument(block);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("block", block.getId());
        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, canvasBlockDocument);
        });
    }

    // 블록 삭제
    public void deleteBlock(Long workspaceId, Long blockId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("block", blockId);
        deleteDocument(alias, documentId);
    }
}
