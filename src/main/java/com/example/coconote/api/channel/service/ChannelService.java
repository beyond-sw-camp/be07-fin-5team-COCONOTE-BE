package com.example.coconote.api.channel.service;

import com.example.coconote.api.channel.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ChannelService {

    private final ChannelRepository channelRepository;
    @Autowired
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }
}
