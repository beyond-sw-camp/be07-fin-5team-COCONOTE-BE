package com.example.coconote.api.thread.tag.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.dto.ThreadSearchResultDto;
import com.example.coconote.api.search.entity.ThreadDocument;
import com.example.coconote.api.search.mapper.ThreadMapper;
import com.example.coconote.api.thread.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.thread.tag.dto.request.TagSearchReqListDto;
import com.example.coconote.api.thread.tag.dto.request.TagUpdateReqDto;
import com.example.coconote.api.thread.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.tag.dto.response.TagSearchListResDto;
import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.tag.repository.TagRepository;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.thread.repository.ThreadRepository;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import com.example.coconote.api.thread.threadTag.repository.ThreadTagRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ChannelRepository channelRepository;
    private final ThreadRepository threadRepository;
    private final ThreadTagRepository threadTagRepository;
    private final ThreadMapper threadMapper;
    private final KafkaTemplate kafkaTemplate;


    public Tag createTag(TagCreateReqDto dto) {
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("Channel not found"));
        Tag tag = tagRepository.save(dto.toEntity(channel));
        return tag;
    }

    public ThreadResDto createAndAddTag(ThreadReqDto dto) {
        Tag tag;
        MessageType messageType;
        if(dto.getTagId()==null){
            messageType = MessageType.CREATE_AND_ADD_TAG;
            Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("Channel not found"));
            tag = tagRepository.save(Tag.builder().name(dto.getTagName()).color(dto.getTagColor()).channel(channel).build());
        } else {
            messageType = MessageType.ADD_TAG;
            tag = tagRepository.findById(dto.getTagId()).orElseThrow(()->new EntityNotFoundException("Tag not found"));
        }
        Thread thread = threadRepository.findById(dto.getThreadId()).orElseThrow(()->new EntityNotFoundException("Thread not found"));
        ThreadTag threadTag = threadTagRepository.save(new ThreadTag(thread, tag));

        ThreadDocument document = threadMapper.toDocument(thread);  // toDocument로 미리 변환
        IndexEntityMessage<ThreadDocument> indexEntityMessage = new IndexEntityMessage<>(thread.getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.THREAD, document);
        kafkaTemplate.send("thread_entity_search", indexEntityMessage.toJson());

        return ThreadResDto.builder()
                .type(messageType)
                .threadTagId(threadTag.getId())
                .id(thread.getId())
                .tagId(tag.getId())
                .tagName(tag.getName())
                .tagColor(tag.getColor())
                .parentThreadId(thread.getParent() != null ? thread.getParent().getId() : null)
                .channelId(thread.getChannel().getChannelId())
                .build();
    }

    public List<TagResDto> tagList(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("Channel not found"));
        List<Tag> tags = tagRepository.findAllByChannelAndIsDeleted(channel, IsDeleted.N);
        List<TagResDto> tagResDtos = tags.stream().map(tag -> tag.fromEntity()).toList();
        return tagResDtos;
    }

    public Tag updateTag(TagUpdateReqDto dto) {
// 1. 태그를 조회하고 이름을 업데이트
        Tag tag = tagRepository.findById(dto.getTagId())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        tag.updateName(dto.getUpdateTagName());
        tagRepository.save(tag);

        // 2. 해당 태그를 참조하는 쓰레드들을 조회
        List<ThreadTag> affectedThreadTags = threadTagRepository.findByTag_Id(tag.getId());
        List<Thread> affectedThreads = affectedThreadTags.stream()
                .map(ThreadTag::getThread)
                .distinct() // 중복 제거
                .toList();

        // 3. 각 쓰레드의 검색 인덱스를 업데이트하여 변경된 태그를 반영
        for (Thread thread : affectedThreads) {
            // ThreadDocument 생성
            ThreadDocument document = threadMapper.toDocument(thread);

            // 검색 인덱스 메시지 생성 및 Kafka로 전송
            IndexEntityMessage<ThreadDocument> indexEntityMessage = new IndexEntityMessage<>(
                    thread.getChannel().getSection().getWorkspace().getWorkspaceId(),
                    EntityType.THREAD,
                    document
            );
            kafkaTemplate.send("thread_entity_search", indexEntityMessage.toJson());
        }

        return tag;
    }

    public void deleteTag(Long tagId) {
        // 1. 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        // 2. 태그를 참조하는 ThreadTag들을 조회
        List<ThreadTag> affectedThreadTags = threadTagRepository.findByTag_Id(tag.getId());
        List<Thread> affectedThreads = affectedThreadTags.stream()
                .map(ThreadTag::getThread)
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        // 3. 관련된 ThreadTag 삭제
        threadTagRepository.deleteAll(affectedThreadTags);

        // 4. 태그를 소프트 삭제
        tag.deleteTag();
        tagRepository.save(tag);

        // 5. 검색 인덱스에서 관련된 쓰레드를 업데이트하여 삭제된 태그를 반영
        for (Thread thread : affectedThreads) {
            // 삭제된 태그를 반영하여 ThreadDocument 생성
            ThreadDocument document = threadMapper.toDocument(thread);

            // 필터링된 태그 목록 생성 (삭제된 태그를 제외)
            List<String> updatedTags = document.getTags().stream()
                    .filter(tagName -> !tagName.equals(tag.getName()))
                    .collect(Collectors.toList());
            document.setTags(updatedTags); // 태그 목록 업데이트

            // 검색 인덱스 메시지 생성 및 Kafka로 전송
            IndexEntityMessage<ThreadDocument> indexEntityMessage = new IndexEntityMessage<>(
                    thread.getChannel().getSection().getWorkspace().getWorkspaceId(),
                    EntityType.THREAD,
                    document
            );
            kafkaTemplate.send("thread_entity_search", indexEntityMessage.toJson());
        }
    }



    @Transactional
    public List<ThreadSearchResultDto> searchTag(Long channelId, List<Long> tagSearchIds) {
        // 입력된 태그의 개수
        Long tagCount = (long) tagSearchIds.size();

        // 모든 태그를 만족하는 쓰레드를 찾습니다.
        List<Thread> threads = threadTagRepository.findThreadsByChannelAndAllTagIds(channelId, tagSearchIds, tagCount);

        // DTO로 변환
        return threads.stream()
                .map(thread -> {
                    WorkspaceMember workspaceMember = thread.getWorkspaceMember();

                    // 태그 정보 변환 - isDeleted가 N인 태그만 포함
                    List<String> tags = thread.getThreadTags().stream()
                            .map(tTag -> tTag.getTag())
                            .filter(tag -> tag.getIsDeleted() == IsDeleted.N) // 삭제되지 않은 태그만 필터링
                            .map(Tag::getName) // 태그 이름만 추출
                            .collect(Collectors.toList());

                    // 파일 정보 변환 - 파일 URL만 포함
                    List<String> fileUrls = thread.getThreadFiles().stream()
                            .map(ThreadFile::getFileURL) // 파일 URL 추출
                            .collect(Collectors.toList());

                    // 결과 DTO 생성
                    return ThreadSearchResultDto.builder()
                            .threadId(String.valueOf(thread.getId()))
                            .content(thread.getContent())
                            .memberName(workspaceMember.getNickname())
                            .profileImageUrl(workspaceMember.getProfileImage())
                            .channelId(thread.getChannel().getChannelId())
                            .createdTime(thread.getCreatedTime().toString())
                            .tags(tags)
                            .fileUrls(fileUrls)
                            .parentThreadId(thread.getParent() != null ? thread.getParent().getId() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
