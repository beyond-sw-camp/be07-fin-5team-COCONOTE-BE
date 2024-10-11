package com.example.coconote.api.section.controller;

import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionSwitchReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.service.SectionService;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SectionController {

    private final SectionService sectionService;
    @Autowired
    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @Operation(summary= "섹션 생성")
    @PostMapping("/section/create")
    public ResponseEntity<Object> sectionCreate(@RequestBody SectionCreateReqDto dto, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        SectionListResDto resDto = sectionService.sectionCreate(dto, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "section is successfully created", resDto);
        System.out.println("commonResDto = " + commonResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

    }

    @Operation(summary= "한 워크스페이스 내 섹션 목록 조회")
    @GetMapping("/section/list/{workspaceId}")
    public ResponseEntity<Object> sectionRead(@PathVariable Long workspaceId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        List<SectionListResDto> dtos = sectionService.sectionList(workspaceId, customPrincipal.getEmail());
        System.out.println("dtos = " + dtos);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "섹션 수정")
    @PatchMapping("/section/update/{id}")
    public ResponseEntity<Object> sectionUpdate(@PathVariable Long id, @RequestBody SectionUpdateReqDto dto, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        SectionListResDto resDto = sectionService.sectionUpdate(id, dto, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully updated", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "섹션 삭제")
    @DeleteMapping("/section/delete/{id}")
    public ResponseEntity<Object> sectionDelete(@PathVariable Long id, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        sectionService.sectionDelete(id, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    @Operation(summary = "한 섹션 내 채널 순서 바꾸기")
//    @PatchMapping("/channel/switch")
//    public ResponseEntity<Object> switchIndex(@RequestBody SectionSwitchReqDto dto, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
//        SectionListResDto resDto = sectionService.switchIndex(dto, customPrincipal.getEmail());
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", resDto);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }

}
