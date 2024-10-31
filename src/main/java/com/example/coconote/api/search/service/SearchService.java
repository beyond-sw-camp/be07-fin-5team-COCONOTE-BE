package com.example.coconote.api.search.service;

import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.search.dto.*;
import com.example.coconote.api.search.entity.*;
import com.example.coconote.api.search.mapper.*;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final CanvasRepository canvasRepository;


    // 워크스페이스 ID를 기반으로 에일리어스를 동적으로 생성
    private String getAliasForWorkspace(Long workspaceId) {
        return "workspace_" + workspaceId;
    }

    // 고유한 documentId 생성 메서드
    private String generateDocumentId(String prefix, Long id) {
        return prefix + "_" + id;
    }

    // 통합 Kafka Listener: 모든 인덱싱 메시지를 처리
    @KafkaListener(topics = {
            "thread_entity_search",
            "workspace_member_entity_search",
            "file_entity_search",
            "channel_entity_search",
            "canvas_block_entity_search",
    })
    public void consumeIndexEntityMessage(String message) {
        log.info("Received Kafka message: {}", message);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            // 1차 파싱하여 entityType 추출
            JsonNode initialNode = objectMapper.readTree(message);
            String entityType = initialNode.path("entityType").asText();

            // entityType이 비어 있을 경우, 이스케이프된 JSON을 해제하여 다시 파싱
            JsonNode jsonNode = entityType.isEmpty()
                    ? objectMapper.readTree(initialNode.asText()) // 이스케이프 해제 및 2차 파싱
                    : initialNode; // 이스케이프되지 않은 JSON 그대로 사용

            // entityType 및 workspaceId 추출
            entityType = jsonNode.path("entityType").asText();
            long workspaceId = jsonNode.path("workspaceId").asLong();
            JsonNode entityNode = jsonNode.get("entity");
            log.info("Deserialized workspaceId: {}, entityType: {}", workspaceId, entityType);

            // entityType에 따른 엔터티 처리
            switch (entityType) {
                case "THREAD" -> indexThread(workspaceId, objectMapper.treeToValue(entityNode, ThreadDocument.class));
                case "WORKSPACE_MEMBER" -> indexWorkspaceMember(workspaceId, objectMapper.treeToValue(entityNode, WorkspaceMemberDocument.class));
                case "FILE" -> indexFileEntity(workspaceId, objectMapper.treeToValue(entityNode, FileEntityDocument.class));
                case "CHANNEL" -> indexChannel(workspaceId, objectMapper.treeToValue(entityNode, ChannelDocument.class));
                case "CANVAS" -> indexCanvas(workspaceId, objectMapper.treeToValue(entityNode, CanvasBlockDocument.class));
                case "BLOCK" -> indexBlock(workspaceId, objectMapper.treeToValue(entityNode, CanvasBlockDocument.class));
                default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
            throw new RuntimeException("Error processing Kafka message", e);
        }
    }


    // 공통 인덱스 저장 메서드
    private <T> void indexDocument(String alias, String documentId, T document) {
        try {
            // 인덱스가 존재하지 않으면 Nori 분석기를 포함하여 생성
            createIndexWithNoriAnalyzerIfNotExists(Long.parseLong(alias.replace("workspace_", "")));

            // 문서를 인덱싱 (documentId가 null일 경우 자동으로 생성됨)
            if (documentId != null) {
                openSearchClient.index(i -> i.index(alias).id(documentId).document(document));
            } else {
                openSearchClient.index(i -> i.index(alias).document(document));  // 자동 생성 ID
            }
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
                                                        // 다른 필드에도 match_phrase_prefix 쿼리 사용
//                                                b.should(sh -> sh.matchPhrasePrefix(m -> m.field(field).query(keyword)));
                                                        // 다른 필드에는 multi_match 쿼리 사용
                                                        b.should(sh -> sh.multiMatch(mm -> mm
                                                                .query(keyword)
                                                                .fields(fields)
                                                                .type(TextQueryType.PhrasePrefix) // TextQueryType 사용
                                                        ));
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
                                            .type(TextQueryType.PhrasePrefix)
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
        List<FileSearchResultDto> files;
        if (!response.hits().hits().isEmpty()) {
            Channel channel = channelRepository.findById(response.hits().hits().get(0).source().getChannelId())
                    .orElseThrow(() -> new EntityNotFoundException("Channel not found with ID: " + response.hits().hits().get(0).source().getChannelId()));

            files = response.hits().hits().stream()
                    .map(document -> FileSearchResultDto.builder()
                            .fileId(document.source().getFileId())
                            .fileName(document.source().getFileName())
                            .fileUrl(document.source().getFileUrl())
                            .folderId(document.source().getFolderId())
                            .channelId(document.source().getChannelId())
                            .channelName(channel.getChannelName())
                            .build())
                    .collect(Collectors.toList());
        } else {
            files = Collections.emptyList();
        }

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
                .map(document -> {
                    Channel channel = channelRepository.findById(document.source().getChannelId())
                            .orElseThrow(() -> new EntityNotFoundException("Channel not found with ID: " + document.source().getChannelId()));
                    // WorkspaceMember 조회
                    Long workspaceMemberId = document.source().getWorkspaceMemberId();
                    WorkspaceMember workspaceMember = workspaceMemberRepository.findById(workspaceMemberId)
                            .orElseThrow(() -> new EntityNotFoundException("Workspace member not found with ID: " + workspaceMemberId));

                    // DTO 생성
                    return ThreadSearchResultDto.builder()
                            .threadId(document.source().getThreadId())
                            .content(document.source().getContent())
                            .memberName(workspaceMember.getNickname()) // WorkspaceMember에서 memberName 설정
                            .profileImageUrl(workspaceMember.getProfileImage()) // WorkspaceMember에서 profileImageUrl 설정
                            .channelId(document.source().getChannelId())
                            .channelName(channel.getChannelName())
                            .createdTime(document.source().getCreatedTime())
                            .parentThreadId(document.source().getParentThreadId())
                            .fileUrls(document.source().getFileUrls())
                            .tags(document.source().getTags())
                            .build();
                })
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
        SearchResponse<CanvasBlockDocument> response = searchDocumentsForMultipleFields(
                alias, keyword, List.of("canvasTitle", "blockContents"), CanvasBlockDocument.class, page, size);

        // DTO로 변환
        List<CanvasBlockSearchResultDto> canvasBlocks = response.hits().hits().stream()
                .map(document -> {
                    CanvasBlockDocument source = document.source();

                    // 채널과 캔버스 조회
                    Channel channel = channelRepository.findById(source.getChannelId())
                            .orElseThrow(() -> new EntityNotFoundException("Channel not found with ID: " + source.getChannelId()));
                    Canvas canvas = canvasRepository.findById(source.getCanvasId())
                            .orElseThrow(() -> new EntityNotFoundException("Canvas not found with ID: " + source.getCanvasId()));

                    // 공통 필드 설정
                    CanvasBlockSearchResultDto.CanvasBlockSearchResultDtoBuilder dtoBuilder = CanvasBlockSearchResultDto.builder()
                            .canvasId(source.getCanvasId())
                            .canvasTitle(canvas.getTitle())
                            .channelId(source.getChannelId())
                            .channelName(channel.getChannelName())
                            .type(source.getType());

                    // type에 따른 개별 필드 설정
                    if ("block".equals(source.getType())) {
                        dtoBuilder.blockId(source.getBlockId())
                                .blockContents(source.getBlockContents());
                    }

                    return dtoBuilder.build();
                })
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

    // Nori 분석기를 적용하여 인덱스를 생성하는 메서드
    private void createIndexWithNoriAnalyzerIfNotExists(Long workspaceId) {
        String alias = getAliasForWorkspace(workspaceId);

        try {
            // 인덱스 존재 여부 확인
            boolean indexExists = openSearchClient.indices().exists(e -> e.index(alias)).value();

            // 인덱스가 존재하지 않으면 생성
            if (!indexExists) {
                // Nori 분석기를 적용한 인덱스 매핑 설정
                openSearchClient.indices().create(c -> c
                        .index(alias)
                        .settings(s -> s
                                .analysis(a -> a
                                        .analyzer("nori_analyzer", na -> na
                                                .custom(ca -> ca
                                                        .tokenizer("nori_tokenizer")
                                                )
                                        )
                                )
                        )
                        .mappings(m -> m
                                .properties("content", p -> p
                                        .text(t -> t
                                                .analyzer("nori_analyzer")
                                        )
                                )
                        )
                );
                log.info("Index created with Nori analyzer: {}", alias);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index with Nori analyzer.", e);
        }
    }


    // 워크스페이스 멤버 인덱스 저장
    @Async
    public void indexWorkspaceMember(Long workspaceId, WorkspaceMemberDocument document) {
        // 인덱스가 존재하지 않으면 Nori 분석기를 적용하여 생성
        createIndexWithNoriAnalyzerIfNotExists(workspaceId);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("workspaceMember", document.getWorkspaceMemberId());  // threadId를 Long으로 변환
        log.info("Indexing Workspace Member - Alias: {}, Document ID: {}", alias, documentId);
        indexDocument(alias, documentId, document);
    }

    // 워크스페이스 멤버 삭제
    public void deleteWorkspaceMember(Long workspaceId, Long workspaceMemberId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("workspaceMember", workspaceMemberId);
        deleteDocument(alias, documentId);
    }

    // 파일 인덱스 저장
    @Async
    public void indexFileEntity(Long workspaceId, FileEntityDocument document) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("fileEntity", Long.valueOf(document.getFileId()));
        indexDocument(alias, documentId, document);

        CompletableFuture.runAsync(() -> {
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
    public void indexChannel(Long workspaceId, ChannelDocument document) {
        createIndexWithNoriAnalyzerIfNotExists(workspaceId);
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("channel", Long.valueOf(document.getChannelId()));
        CompletableFuture.runAsync(() -> {
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
    public void indexThread(Long workspaceId, ThreadDocument document) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("thread", Long.valueOf(document.getThreadId()));  // threadId를 Long으로 변환
        CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);
        });
    }

    // 쓰레드 삭제
    public void deleteThread(Long workspaceId, Long threadId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("thread", threadId);
        deleteDocument(alias, documentId);
    }

    @Async
    public void indexCanvas(Long workspaceId, CanvasBlockDocument document) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("canvas", document.getCanvasId());
        CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);  // documentId 없이 호출하여 자동 생성
        });
    }

    // 캔버스 삭제
    public void deleteCanvas(Long workspaceId, Long canvasId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("canvas", canvasId);
        deleteDocument(alias, documentId);
    }

    @Async
    public CompletableFuture<Void> indexBlock(Long workspaceId, CanvasBlockDocument document) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("block", document.getBlockId());

        return CompletableFuture.runAsync(() -> {
            indexDocument(alias, documentId, document);  // documentId 없이 호출하여 자동 생성
        });
    }


    // 블록 삭제
    public void deleteBlock(Long workspaceId, Long blockId) {
        String alias = getAliasForWorkspace(workspaceId);
        String documentId = generateDocumentId("block", blockId);
        deleteDocument(alias, documentId);
    }


    public SearchResultWithTotal<ThreadSearchResultDto> searchThreadsByTags(Long workspaceId, List<String> tags, int page, int size) {
        String alias = getAliasForWorkspace(workspaceId);

        try {
            // OpenSearch 쿼리 빌더에서 태그 조건을 추가
            SearchResponse<ThreadDocument> response = openSearchClient.search(s -> s
                            .index(alias)
                            .from(page * size) // 페이징 처리 (시작 위치)
                            .size(size) // 한 페이지에 반환할 결과 개수
                            .query(q -> q
                                    .bool(b -> {
                                        // for문을 사용하여 각각의 태그에 대해 should 조건 추가
                                        for (String tag : tags) {
                                            b.should(sq -> sq
                                                    .term(t -> t
                                                            .field("tags.keyword") // 분석되지 않은 필드 사용 (예: tags.keyword)
                                                            .value(FieldValue.of(tag))
                                                    )
                                            );
                                        }
                                        // 최소한 하나 이상의 태그가 일치하는 결과만 반환
                                        b.minimumShouldMatch(String.valueOf(tags.size())); // 최소 일치 개수를 1로 설정
                                        return b;
                                    })
                            ),
                    ThreadDocument.class
            );

            // 검색 결과를 DTO로 변환
            List<ThreadSearchResultDto> threads = response.hits().hits().stream()
                    .map(document -> {
                        // WorkspaceMember 조회
                        Long workspaceMemberId = document.source().getWorkspaceMemberId();
                        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(workspaceMemberId)
                                .orElseThrow(() -> new EntityNotFoundException("Workspace member not found with ID: " + workspaceMemberId));

                        // 자식 쓰레드 정보 변환
                        List<ThreadSearchResultDto> childThreadDtos = document.source().getChildThreads().stream()
                                .map(child -> {
                                    // 자식 쓰레드의 WorkspaceMember 조회
                                    Long childWorkspaceMemberId = child.getWorkspaceMemberId();
                                    WorkspaceMember childWorkspaceMember = workspaceMemberRepository.findById(childWorkspaceMemberId)
                                            .orElseThrow(() -> new EntityNotFoundException("Workspace member not found with ID: " + childWorkspaceMemberId));

                                    // 자식 쓰레드 DTO 생성
                                    return ThreadSearchResultDto.builder()
                                            .threadId(child.getThreadId())
                                            .content(child.getContent())
                                            .memberName(childWorkspaceMember.getNickname()) // 자식 쓰레드의 memberName 설정
                                            .profileImageUrl(childWorkspaceMember.getProfileImage()) // 자식 쓰레드의 profileImageUrl 설정
                                            .channelId(child.getChannelId())
                                            .createdTime(child.getCreatedTime())
                                            .tags(child.getTags())
                                            .fileUrls(child.getFileUrls())
                                            .parentThreadId(child.getParentThreadId())
                                            .build();
                                })
                                .collect(Collectors.toList());

                        // 상위 쓰레드 DTO 생성
                        return ThreadSearchResultDto.builder()
                                .threadId(document.source().getThreadId())
                                .content(document.source().getContent())
                                .memberName(workspaceMember.getNickname()) // WorkspaceMember에서 memberName 설정
                                .profileImageUrl(workspaceMember.getProfileImage()) // WorkspaceMember에서 profileImageUrl 설정
                                .channelId(document.source().getChannelId())
                                .createdTime(document.source().getCreatedTime())
                                .tags(document.source().getTags())
                                .fileUrls(document.source().getFileUrls()) // 파일 URL 추가
                                .childThreads(childThreadDtos) // 자식 쓰레드 추가
                                .parentThreadId(document.source().getParentThreadId()) // 부모 쓰레드 ID 추가
                                .build();
                    })
                    .collect(Collectors.toList());

            return new SearchResultWithTotal<>(threads, response.hits().total().value());

        } catch (IOException e) {
            throw new RuntimeException("OpenSearch 검색 중 오류가 발생했습니다.", e);
        }
    }
}