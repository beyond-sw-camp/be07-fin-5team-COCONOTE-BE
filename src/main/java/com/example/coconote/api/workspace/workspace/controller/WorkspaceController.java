package com.example.coconote.api.workspace.workspace.controller;

import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.workspace.service.WorkspaceService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    @Autowired
    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Operation(summary= "워크스페이스 생성")
    @PostMapping("/workspace/create")
    public ResponseEntity<Object> workspaceCreate(@RequestBody WorkspaceCreateReqDto dto) {
            WorkspaceListResDto resDto = workspaceService.workspaceCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "workspace is successfully created", resDto);
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "워크스페이스 목록 조회")
    @GetMapping("/workspace/list")
    public ResponseEntity<Object> workspaceList() {
        List<WorkspaceListResDto> dtos = workspaceService.workspaceList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "워크스페이스내 모든 섹션 및 채널 조회")
    @GetMapping("/workspace/list/{workspaceId}")
    public ResponseEntity<Object> workspaceRead(@PathVariable Long workspaceId) {
        List<SectionListResDto> dtos = workspaceService.workspaceRead(workspaceId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "workspace is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "워크스페이스 수정")
    @PatchMapping("/workspace/update/{id}")
    public ResponseEntity<Object> workspaceUpdate(@PathVariable Long id, @RequestBody WorkspaceUpdateReqDto dto) {
        WorkspaceListResDto resDto = workspaceService.workspaceUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "workspace is successfully updated", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "워크스페이스 삭제")
    @DeleteMapping("/workspace/delete/{id}")
    public ResponseEntity<Object> workspaceDelete(@PathVariable Long id) {
        workspaceService.workspaceDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "workspace is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }












}
