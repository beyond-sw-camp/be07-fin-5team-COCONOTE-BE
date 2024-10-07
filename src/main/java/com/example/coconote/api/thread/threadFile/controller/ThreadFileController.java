package com.example.coconote.api.thread.threadFile.controller;

import com.example.coconote.api.thread.threadFile.service.ThreadFileService;
import com.example.coconote.common.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/thread/file")
public class ThreadFileController {
    private final ThreadFileService threadFileService;

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteThreadFile(@PathVariable Long fileId) {
        threadFileService.deleteFile(fileId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "파일이 성공적으로 삭제되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
