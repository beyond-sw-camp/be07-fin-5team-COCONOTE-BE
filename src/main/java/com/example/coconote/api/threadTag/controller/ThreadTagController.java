package com.example.coconote.api.threadTag.controller;

import com.example.coconote.api.threadTag.dto.ThreadTagReqDto;
import com.example.coconote.api.threadTag.service.ThreadTagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/threadtag")
public class ThreadTagController {

    private final ThreadTagService threadTagService;

    @Operation(summary= "쓰레드에 태그 추가")
    @PostMapping("add")
    public ResponseEntity<?> addThreadTag(@RequestBody ThreadTagReqDto dto) {
        threadTagService.addThreadTag(dto);
        return new ResponseEntity<>("태그 추가 완료", HttpStatus.OK);
    }

    @Operation(summary= "쓰레드에 태그 삭제")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteThreadTag(@PathVariable Long id) {
        threadTagService.deleteThreadTag(id);
        return new ResponseEntity<>("태그 삭제 완료", HttpStatus.OK);
    }
}
