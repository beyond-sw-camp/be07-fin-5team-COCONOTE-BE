package com.example.coconote.api.channel.controller;

import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.service.ChannelService;
import com.example.coconote.api.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.common.CommonErrorDto;
import com.example.coconote.common.CommonResDto;
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

    @PostMapping("/channel/create") // 채널 생성
    public ResponseEntity<Object> channelCreate(@RequestBody ChannelCreateReqDto dto) {
        try {
            Channel channel = channelService.channelCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "channel is successfully created", channel.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/channel/list")
    public ResponseEntity<Object> channelRead() {
        List<ChannelListResDto> dtos = channelService.channelList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/channel/update/{id}")
    public ResponseEntity<Object> channelUpdate(@PathVariable Long id, @RequestBody ChannelUpdateReqDto dto) {
        try {
            channelService.channelUpdate(id, dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channel is successfully updated", null);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/channel/delete/{id}") // 댓글 삭제
    public ResponseEntity<Object> channelDelete(@PathVariable Long id) {
        try {
            channelService.channelDelete(id);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channel is successfully deleted", null);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

}
