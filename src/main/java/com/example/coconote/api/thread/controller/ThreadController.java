package com.example.coconote.api.thread.controller;


import com.example.coconote.api.drive.dto.response.FolderCreateResDto;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadListResDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.service.ThreadService;
import com.example.coconote.common.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/thread")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PostMapping("create")
    public ResponseEntity<?> createThread(@RequestBody ThreadCreateReqDto dto) {
        Thread thread = threadService.createThread(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "쓰레드가 성공적으로 생성되었습니다.", thread.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("list/{channelId}")
    public ResponseEntity<?> listThreads(@PathVariable Long channelId, Pageable pageable) {
        Page<ThreadListResDto> ThreadListResDtos = threadService.threadList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "쓰레드 목록이 성공적으로 반환되었습니다.", ThreadListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
