package com.example.coconote.api.tag.controller;


import com.example.coconote.api.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.tag.dto.request.TagUpdateReqDto;
import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.tag.entity.Tag;
import com.example.coconote.api.tag.service.TagService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary= "태그 생성")
    @PostMapping("create")
    public ResponseEntity<?> createTag(@RequestBody TagCreateReqDto dto) {
        Tag tag = tagService.createTag(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "태그가 성공적으로 생성되었습니다.", tag.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "태그 목록 반환")
    @GetMapping("list/{channelId}")
    public ResponseEntity<?> listTags(@PathVariable Long channelId) {
        List<TagResDto> tagResDtos = tagService.tagList(channelId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "태그 목록 반환 성공.", tagResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "태그 이름 변경")
    @PostMapping("update")
    public ResponseEntity<?> updateTag(@RequestBody TagUpdateReqDto dto){
        Tag tag = tagService.updateTag(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "태그 이름 변경 성공.", tag.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "태그 삭제")
    @DeleteMapping("delete/{tagId}")
    public ResponseEntity<?> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return new ResponseEntity<>("태그 삭제 성공", HttpStatus.OK);
    }
}
