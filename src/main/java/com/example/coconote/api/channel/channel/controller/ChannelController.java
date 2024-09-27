package com.example.coconote.api.channel.channel.controller;

import com.example.coconote.api.channel.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.service.ChannelService;
import com.example.coconote.api.drive.dto.response.FolderAllListResDto;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.entity.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ChannelController {

    private final ChannelService channelService;
    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Operation(summary = "채널 생성")
    @PostMapping("/channel/create")
    public ResponseEntity<Object> channelCreate(@RequestBody ChannelCreateReqDto dto, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        ChannelDetailResDto resDto = channelService.channelCreate(dto, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "channel is successfully created", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "한 섹션 내 채널 목록 조회")
    @GetMapping("/channel/list/{sectionId}")
    public ResponseEntity<Object> channelRead(@PathVariable Long sectionId) {
        List<ChannelDetailResDto> dtos = channelService.channelList(sectionId);
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

    @Operation(summary= "채널 에 속한 드라이브로 이동")
    @GetMapping("/channel/{id}/drive")
    public ResponseEntity<Object> channelDrive(@PathVariable Long id, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        FolderAllListResDto resDto = channelService.channelDrive(id, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channel is successfully moved to drive", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
