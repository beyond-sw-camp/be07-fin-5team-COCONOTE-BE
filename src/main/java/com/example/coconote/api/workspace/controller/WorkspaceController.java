package com.example.coconote.api.workspace.controller;

import com.example.coconote.api.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspace.service.WorkspaceService;
import com.example.coconote.common.CommonErrorDto;
import com.example.coconote.common.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
        try {
            Workspace workspace = workspaceService.workspaceCreate(dto, imgFile);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "workspace is successfully created", workspace.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }




}
