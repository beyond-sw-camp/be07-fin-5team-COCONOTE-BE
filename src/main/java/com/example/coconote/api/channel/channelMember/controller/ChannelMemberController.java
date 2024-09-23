package com.example.coconote.api.channel.channelMember.controller;

import com.example.coconote.api.channel.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.channel.channelMember.service.ChannelMemberService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChannelMemberController {
    private final ChannelMemberService channelMemberService;
    @Autowired
    public ChannelMemberController(ChannelMemberService channelMemberService) {
        this.channelMemberService = channelMemberService;
    }

    @Operation(summary= "채널 회원 생성(초대/가입)")
    @PostMapping("/channelMember/create/{channelId}") // 채널 가입
    public ResponseEntity<Object> channelMemberCreate(@PathVariable Long channelId) {
        ChannelMemberListResDto resDto = channelMemberService.channelMemberCreate(channelId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "채널 회원 목록 조회")
    @GetMapping("/channelMember/list/{channelId}") // 채널 유저 목록 조회
    public ResponseEntity<Object> channelMemberRead(@PathVariable Long channelId) {
        List<ChannelMemberListResDto> dtos = channelMemberService.channelMemberList(channelId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "채널 관리자 권한 부여/박탈")
    @PatchMapping("/channelMember/role/{id}") // 채널 관리자 권한 부여/삭제
    public ResponseEntity<Object> channelMemberChangeRole(@PathVariable Long id) {
        ChannelMemberListResDto resDto = channelMemberService.channelMemberChangeRole(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "role is successfully changed", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


//    @PatchMapping("/channelMember/bookmark/{id}")  // 채널 즐겨찾기
//    public ResponseEntity<Object> channelUpdate(@PathVariable Long id, @RequestBody ChannelUpdateReqDto dto) {
//        channelMemberService.channelMemberUpdate(id, dto);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "bookmark is successfully added", null);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
//
//
//    @PatchMapping("/channelMember/delete/{id}") // 채널 강퇴
//    public ResponseEntity<Object> channelDelete(@PathVariable Long id) {
//        channelMemberService.channelMemberDelete(id);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channelMember is successfully deleted", null);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
}



