package com.example.coconote.api.section.controller;

import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.service.SectionService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> sectionCreate(@RequestBody SectionCreateReqDto dto) {
        SectionListResDto resDto = sectionService.sectionCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "section is successfully created", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

    }

    @Operation(summary= "섹션 조회")
    @GetMapping("/section/list/{workspaceId}")
    public ResponseEntity<Object> sectionRead(@PathVariable Long workspaceId) {
        List<SectionListResDto> dtos = sectionService.sectionList(workspaceId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "섹션 수정")
    @PatchMapping("/section/update/{id}")
    public ResponseEntity<Object> sectionUpdate(@PathVariable Long id, SectionUpdateReqDto dto) {
        SectionListResDto resDto = sectionService.sectionUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully updated", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "섹션 삭제")
    @DeleteMapping("/section/delete/{id}")
    public ResponseEntity<Object> sectionDelete(@PathVariable Long id) {
        sectionService.sectionDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
