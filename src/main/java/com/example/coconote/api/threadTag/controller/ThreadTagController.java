package com.example.coconote.api.threadTag.controller;

import com.example.coconote.api.threadTag.dto.ThreadTagAddReqDto;
import com.example.coconote.api.threadTag.service.ThreadTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/threadtag")
public class ThreadTagController {

    private final ThreadTagService threadTagService;

    @PostMapping("add")
    public void addThreadTag(@RequestBody ThreadTagAddReqDto dto) {
        threadTagService.addThreadTag(dto);
    }
}
