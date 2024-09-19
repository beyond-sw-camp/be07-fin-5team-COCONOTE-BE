package com.example.coconote.api.canvas.service;

import com.example.coconote.api.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.api.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.canvas.repository.CanvasRepository;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import org.springframework.stereotype.Service;

@Service
public class CanvasService {
    private final CanvasRepository canvasRepository;
    private final ChannelRepository channelRepository;

    public CanvasService(CanvasRepository canvasRepository, ChannelRepository channelRepository){

        this.canvasRepository = canvasRepository;
        this.channelRepository = channelRepository;
    }

    public CreateCanvasResDto createCanvas(CreateCanvasReqDto createCanvasReqDto, String email){
        Channel channel = channelRepository.findById(createCanvasReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Canvas parentCanvas = null;
        // 부모 캔버스 조회 (parentCanvasId가 null이 아닐 경우에만)
        if (createCanvasReqDto.getParentCanvasId() != null) {
            parentCanvas = canvasRepository.findById(createCanvasReqDto.getParentCanvasId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 캔버스가 존재하지 않습니다."));
            if (!parentCanvas.getChannel().getId().equals(channel.getId())) {
                throw new IllegalArgumentException("부모 캔버스가 다른 채널에 있습니다.");
            }
        }

        Canvas canvas = Canvas.builder()
                .title(createCanvasReqDto.getTitle())
                .parentCanvas(parentCanvas)
                .channel(channel)
                .build();

        canvasRepository.save(canvas);
        return CreateCanvasResDto.fromEntity(canvas);
    }
}
