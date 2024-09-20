package com.example.coconote.api.channel.service;

import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.entity.Workspace;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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

    public List<ChannelListResDto> channelList() {
        List<Channel> channels = channelRepository.findAll();
        List<ChannelListResDto> dtos = new ArrayList<>();
        for(Channel c : channels) {
            dtos.add(c.fromEntity());
        }
        return dtos;
    }

    public Channel channelUpdate(Long id, ChannelUpdateReqDto dto) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException(" 찾을 수 없습니다."));
        channel.updateEntity(dto);
        return channel;
    }

    public void channelDelete(Long id) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        channel.deleteEntity();
    }
}
