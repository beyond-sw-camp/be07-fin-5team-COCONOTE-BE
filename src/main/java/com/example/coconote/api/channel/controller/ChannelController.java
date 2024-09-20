package com.example.coconote.api.channel.controller;

import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.service.ChannelService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChannelController {

    private final ChannelService channelService;
    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Operation(summary= "채널 생성")
    @PostMapping("/channel/create")
    public ResponseEntity<Object> channelCreate(@RequestBody ChannelCreateReqDto dto) {
        Channel channel = channelService.channelCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "channel is successfully created", channel);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "채널 조회")
    @GetMapping("/channel/list")
    public ResponseEntity<Object> channelRead() {
        List<ChannelListResDto> dtos = channelService.channelList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "채널 수정")
    @PatchMapping("/channel/update/{id}")
    public ResponseEntity<Object> channelUpdate(@PathVariable Long id, @RequestBody ChannelUpdateReqDto dto) {
        Channel channel = channelService.channelUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channel is successfully updated", channel);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "채널 삭제")
    @DeleteMapping("/channel/delete/{id}") // 댓글 삭제
    public ResponseEntity<Object> channelDelete(@PathVariable Long id) {
        channelService.channelDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channel is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
