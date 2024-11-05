package com.example.coconote.api.thread.tag.controller;


import com.example.coconote.api.search.dto.ThreadSearchResultDto;
import com.example.coconote.api.thread.tag.dto.request.TagCreateReqDto;
import com.example.coconote.api.thread.tag.dto.request.TagSearchReqListDto;
import com.example.coconote.api.thread.tag.dto.request.TagUpdateReqDto;
import com.example.coconote.api.thread.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.tag.dto.response.TagSearchListResDto;
import com.example.coconote.api.thread.tag.entity.Tag;
import com.example.coconote.api.thread.tag.service.TagService;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @Operation(summary = "태그 검색")
    @GetMapping("/search")
    public ResponseEntity<?> searchTag(@RequestParam Long channelId, @RequestParam List<Long> tagSearchIds) {
        // 서비스 레이어에서 변환된 결과 가져오기
        List<ThreadSearchResultDto> threadResults = tagService.searchTag(channelId, tagSearchIds);

        // 응답을 `CommonResDto` 형식으로 래핑하여 반환
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "태그 검색 성공", threadResults);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
