package com.example.coconote.api.section.controller;

import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.service.SectionService;
import com.example.coconote.common.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SectionController {

    private final SectionService sectionService;
    @Autowired
    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping("/section/create") // 섹션 생성
    public ResponseEntity<Object> sectionCreate(@RequestBody SectionCreateReqDto dto) {
        Section section = sectionService.sectionCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "section is successfully created", section.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

    }

    @GetMapping("/section/list")
    public ResponseEntity<Object> sectionRead() {
        List<SectionListResDto> dtos = sectionService.sectionList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/section/update/{id}")
    public ResponseEntity<Object> sectionUpdate(@PathVariable Long id, SectionUpdateReqDto dto) {
        sectionService.sectionUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully updated", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @PatchMapping("/section/delete/{id}")
    public ResponseEntity<Object> sectionDelete(@PathVariable Long id) {
        sectionService.sectionDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "section is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
