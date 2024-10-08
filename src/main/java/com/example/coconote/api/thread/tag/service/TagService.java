package com.example.coconote.api.thread.tag.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.thread.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.thread.tag.dto.request.TagUpdateReqDto;
import com.example.coconote.api.thread.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.tag.repository.TagRepository;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.thread.repository.ThreadRepository;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import com.example.coconote.api.thread.threadTag.repository.ThreadTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ChannelRepository channelRepository;
    private final ThreadRepository threadRepository;
    private final ThreadTagRepository threadTagRepository;

    public Tag createTag(TagCreateReqDto dto) {
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("Channel not found"));
        Tag tag = tagRepository.save(dto.toEntity(channel));
        return tag;
    }

    public ThreadResDto createAndAddTag(ThreadReqDto dto) {
        Tag tag;
        if(dto.getTagId()==null){
            Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("Channel not found"));
            tag = tagRepository.save(Tag.builder().name(dto.getTagName()).color(dto.getTagColor()).channel(channel).build());
        } else {
            tag = tagRepository.findById(dto.getTagId()).orElseThrow(()->new EntityNotFoundException("Tag not found"));
        }
        Thread thread = threadRepository.findById(dto.getThreadId()).orElseThrow(()->new EntityNotFoundException("Thread not found"));
        threadTagRepository.save(new ThreadTag(thread, tag));
        return ThreadResDto.builder()
                .type(MessageType.ADD_TAG)
                .id(thread.getId())
                .tagId(tag.getId())
                .tagName(tag.getName())
                .tagColor(tag.getColor())
                .build();
    }

    public List<TagResDto> tagList(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("Channel not found"));
        List<Tag> tags = tagRepository.findAllByChannel(channel);
        List<TagResDto> tagResDtos = tags.stream().map(tag -> tag.fromEntity()).toList();
        return tagResDtos;
    }

    public Tag updateTag(TagUpdateReqDto dto) {
        Tag tag = tagRepository.findById(dto.getTagId()).orElseThrow(()->new EntityNotFoundException("Tag not found"));
        tag.updateName(dto.getUpdateTagName());
        return tag;
    }

    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId).orElseThrow(()->new EntityNotFoundException("Tag not found"));
        tag.deleteTag();
    }
}
