package com.example.coconote.api.tag.controller;


import com.example.coconote.api.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.tag.dto.response.TagListResDto;
import com.example.coconote.api.tag.entity.Tag;
import com.example.coconote.api.tag.service.TagService;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.service.ThreadService;
import com.example.coconote.common.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @PostMapping("create")
    public ResponseEntity<?> createTag(@RequestBody TagCreateReqDto dto) {
        Tag tag = tagService.createTag(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "태그가 성공적으로 생성되었습니다.", tag.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("list/{channelId}")
    public ResponseEntity<?> listTags(@PathVariable Long channelId) {
        List<TagListResDto> tagListResDtos = tagService.tagList(channelId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "태그 목록 반환 성공.", tagListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
