package com.example.coconote.api.tag.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.tag.dto.request.TagUpdateReqDto;
import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.tag.entity.Tag;
import com.example.coconote.api.tag.repository.TagRepository;
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

    public Tag createTag(TagCreateReqDto dto) {
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(()->new EntityNotFoundException("Channel not found"));
        Tag tag = tagRepository.save(dto.toEntity(channel));
        return tag;
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
