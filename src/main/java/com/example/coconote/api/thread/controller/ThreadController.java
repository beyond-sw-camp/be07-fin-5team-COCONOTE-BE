package com.example.coconote.api.thread.controller;


import com.example.coconote.api.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.service.ThreadService;
import com.example.coconote.common.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/thread")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PostMapping("/create")
    public ResponseEntity<?> createThread(@RequestBody ThreadReqDto dto) {
        ThreadResDto threadResDto = threadService.createThread(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "쓰레드가 성공적으로 생성되었습니다.", threadResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateThread(@RequestBody ThreadReqDto dto) {
        ThreadResDto threadResDto = threadService.updateThread(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드가 성공적으로 수정되었습니다.", threadResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/list/{channelId}")
    public ResponseEntity<?> listThreads(@PathVariable Long channelId, Pageable pageable) {
        Page<ThreadResDto> ThreadListResDtos = threadService.threadList(channelId, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드 목록이 성공적으로 반환되었습니다.", ThreadListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{threadId}")
    public ResponseEntity<?> deleteThread(@PathVariable Long threadId) {
        threadService.deleteThread(threadId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드가 성공적으로 삭제되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
