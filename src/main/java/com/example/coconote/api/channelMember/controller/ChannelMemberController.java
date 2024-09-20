//package com.example.coconote.api.channelMember.controller;
//
//import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
//import com.example.coconote.api.channel.dto.request.ChannelUpdateReqDto;
//import com.example.coconote.api.channel.dto.response.ChannelListResDto;
//import com.example.coconote.api.channelMember.dto.response.ChannelMemberListResDto;
//import com.example.coconote.api.channelMember.entity.ChannelMember;
//import com.example.coconote.api.channelMember.service.ChannelMemberService;
//import com.example.coconote.common.CommonResDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//public class ChannelMemberController {
//    private final ChannelMemberService channelMemberService;
//    @Autowired
//    public ChannelMemberController(ChannelMemberService channelMemberService) {
//        this.channelMemberService = channelMemberService;
//    }
//
//    @PostMapping("/channelMember/create/{channelId}") // 채널 가입
//    public ResponseEntity<Object> channelMemberCreate(@PathVariable Long channelId) {
//        ChannelMember channelMember = channelMemberService.channelMemberCreate(channelId);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "channelMember is successfully created", channelMember.getId());
//        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
//    }

//    @GetMapping("/channelMember/list/{channelId}") // 채널 유저 목록 조회
//    public ResponseEntity<Object> channelMemberRead(@PathVariable Long channelId) {
//        List<ChannelMemberListResDto> dtos = channelMemberService.channelMemberList(channelId);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
//
//    @PatchMapping("/channelMember/role/{id}") // 채널 관리자
//    public ResponseEntity<Object> channelUpdate(@PathVariable Long id, @RequestBody ChannelUpdateReqDto dto) {
//        channelMemberService.channelMemberUpdate(id, dto);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channelMember is successfully updated", null);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
//
//
//    @PatchMapping("/channelMember/bookmark/{id}") // 채널 관리자 권한 부여/삭제
//    public ResponseEntity<Object> channelUpdate(@PathVariable Long id, @RequestBody ChannelUpdateReqDto dto) {
//        channelMemberService.channelMemberUpdate(id, dto);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channelMember is successfully updated", null);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
//
//
//    // 채널 즐겨찾기
//
//
//    @PatchMapping("/channelMember/delete/{id}") // 채널 강퇴
//    public ResponseEntity<Object> channelDelete(@PathVariable Long id) {
//        channelMemberService.channelMemberDelete(id);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "channelMember is successfully deleted", null);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }
//}



