package com.example.coconote.api.thread.thread.controller;


import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.thread.service.ThreadService;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.entity.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/thread")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @Operation(summary= "쓰레드 생성")
    @PostMapping("/create")
    public ResponseEntity<?> createThread(@RequestBody ThreadReqDto dto, @AuthenticationPrincipal CustomPrincipal member) {
        ThreadResDto threadResDto = threadService.createThread(dto, member.getMemberId());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "쓰레드가 성공적으로 생성되었습니다.", threadResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }
    
    @Operation(summary= "쓰레드 수정")
    @PostMapping("/update")
    public ResponseEntity<?> updateThread(@RequestBody ThreadReqDto dto) {
        ThreadResDto threadResDto = threadService.updateThread(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드가 성공적으로 수정되었습니다.", threadResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "쓰레드 목록 반환")
    @GetMapping("/list/{channelId}")
    public ResponseEntity<?> listThreads(@PathVariable Long channelId, Pageable pageable, @AuthenticationPrincipal CustomPrincipal member) {
        Page<ThreadResDto> ThreadListResDtos = threadService.threadList(channelId, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드 목록이 성공적으로 반환되었습니다.", ThreadListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "쓰레드 삭제")
    @DeleteMapping("/delete/{threadId}")
    public ResponseEntity<?> deleteThread(@PathVariable Long threadId) {
        threadService.deleteThread(threadId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드가 성공적으로 삭제되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
