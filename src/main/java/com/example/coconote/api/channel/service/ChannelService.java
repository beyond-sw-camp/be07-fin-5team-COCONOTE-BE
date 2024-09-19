package com.example.coconote.api.channel.service;

import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class ChannelService {

    private final ChannelRepository channelRepository;
    private final SectionRepository sectionRepository;
    @Autowired
    public ChannelService(ChannelRepository channelRepository, SectionRepository sectionRepository) {
        this.channelRepository = channelRepository;
        this.sectionRepository = sectionRepository;
    }


    public Channel channelCreate(ChannelCreateReqDto dto) {
        Section section = sectionRepository.findById(dto.getSectionId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 섹션입니다."));

        Channel channel = dto.toEntity(section);
        channelRepository.save(channel);

        return channel;
    }
}
