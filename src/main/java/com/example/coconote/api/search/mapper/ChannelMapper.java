package com.example.coconote.api.search.mapper;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.search.entity.ChannelDocument;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

    public ChannelDocument toDocument(Channel channel) {
        return ChannelDocument.builder()
                .channelId(String.valueOf(channel.getChannelId()))
                .channelName(channel.getChannelName())
                .channelInfo(channel.getChannelInfo())
                .isPublic(channel.getIsPublic())
                .sectionId(channel.getSection().getSectionId())
                .build();
    }
}

