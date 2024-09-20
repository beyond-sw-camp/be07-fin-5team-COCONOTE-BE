package com.example.coconote.api.workspace.controller;

import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.dto.request.WorkspaceUpdateReqDto;
import com.example.coconote.api.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.service.WorkspaceService;
import com.example.coconote.common.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    @Autowired
    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping("/workspace/create")
    public ResponseEntity<Object> workspaceCreate(@RequestPart(value = "data") WorkspaceCreateReqDto dto,
                                                  @RequestPart(value = "file") MultipartFile imgFile) {
            Workspace workspace = workspaceService.workspaceCreate(dto, imgFile);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "workspace is successfully created", workspace.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }


    @GetMapping("/workspace/list")
    public ResponseEntity<Object> workspaceRead() {
        List<WorkspaceListResDto> dtos = workspaceService.workspaceList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/workspace/update/{id}")
    public ResponseEntity<Object> workspaceUpdate(@PathVariable Long id, @RequestBody WorkspaceUpdateReqDto dto) {
        workspaceService.workspaceUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "workspace is successfully updated", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/workspace/delete/{id}")
    public ResponseEntity<Object> workspaceDelete(@PathVariable Long id) {
        workspaceService.workspaceDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "workspace is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }












}