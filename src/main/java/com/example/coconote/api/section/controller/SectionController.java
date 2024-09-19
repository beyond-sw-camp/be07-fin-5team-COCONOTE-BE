package com.example.coconote.api.section.controller;

import com.example.coconote.api.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.service.SectionService;
import com.example.coconote.api.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.common.CommonErrorDto;
import com.example.coconote.common.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        try {
            Section section = sectionService.sectionCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "section is successfully created", section.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/section/list")
    public ResponseEntity<Object> sectionRead() {
        List<SectionListResDto> dtos = sectionService.sectionList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
