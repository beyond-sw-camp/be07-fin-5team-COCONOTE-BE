package com.example.coconote.api.drive.controller;

import com.example.coconote.api.drive.dto.request.CreateFolderReqDto;
import com.example.coconote.api.drive.dto.response.FolderChangeNameResDto;
import com.example.coconote.api.drive.dto.response.FolderCreateResDto;
import com.example.coconote.api.drive.service.FolderService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drive/folder")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @Operation(
            summary = "폴더 생성",
            description = "새로운 폴더를 생성. `parentFolderId`가 null이면 최상위 폴더로 간주"
    )
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestBody CreateFolderReqDto createFolderReqDto, String email){
        FolderCreateResDto folderCreateResDto = folderService.createFolder(createFolderReqDto, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "폴더가 성공적으로 생성되었습니다..", folderCreateResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{folderId}/update/name")
    public ResponseEntity<?> updateFolderName(@PathVariable Long folderId, @RequestParam String folderName, String email){
        FolderChangeNameResDto folderChangeNameResDto = folderService.updateFolderName(folderId, folderName, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "폴더 이름이 성공적으로 수정되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long folderId, String email){
        folderService.deleteFolder(folderId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "폴더가 성공적으로 삭제되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



}
