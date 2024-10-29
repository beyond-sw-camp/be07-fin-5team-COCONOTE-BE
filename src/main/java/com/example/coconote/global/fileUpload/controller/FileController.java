package com.example.coconote.global.fileUpload.controller;

import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.global.fileUpload.dto.request.*;
import com.example.coconote.global.fileUpload.dto.response.FileMetadataResDto;
import com.example.coconote.global.fileUpload.dto.response.FolderLocationResDto;
import com.example.coconote.global.fileUpload.dto.response.MoveFileResDto;
import com.example.coconote.global.fileUpload.service.S3Service;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/presigned-urls")
    public ResponseEntity<?> generatePresignedUrls(@RequestBody List<FileUploadRequest> files, @AuthenticationPrincipal CustomPrincipal member) {
        Map<String, String> presignedUrls = s3Service.generatePresignedUrls(files, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Presigned URLs generated successfully", presignedUrls);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 2. 파일 메타데이터 저장 API
    @Operation(summary = "파일 URL 저장 API, Channel ID는 필수, Folder ID는 선택(없으면 null 로 보내면 됨)")
    @PostMapping("/metadata")
    public ResponseEntity<?> saveFileMetadata(@RequestBody FileMetadataReqDto fileMetadataList, @AuthenticationPrincipal CustomPrincipal member) {
        List<FileMetadataResDto> savedMetadata = s3Service.saveFileMetadata(fileMetadataList, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "File metadata saved successfully", savedMetadata);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary = "프로필 이미지 URL 저장 API")
    @PostMapping("/profile")
    public ResponseEntity<?> saveProfileImage(@RequestBody ProfileImageReqDto profileImageReqDto, @AuthenticationPrincipal CustomPrincipal member) {
        WorkspaceMemberResDto savedMetadata = s3Service.saveProfileImage(profileImageReqDto, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "Profile image saved successfully", savedMetadata);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    // 3. 파일 삭제 API
    @Operation(summary = "파일 삭제 API (삭제 가능한 사람은 파일을 업로드한 사람 또는 채널의 관리자)")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, @AuthenticationPrincipal CustomPrincipal member) {
        s3Service.deleteFile(fileId,member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File deleted successfully", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    // 4. 파일 이동 API
    @Operation(summary = "파일 이동 API ")
    @PatchMapping("/move")
    public ResponseEntity<?> moveFile(@RequestBody MoveFileReqDto moveFileReqDto, @AuthenticationPrincipal CustomPrincipal member) {
        MoveFileResDto response = s3Service.moveFile(moveFileReqDto, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File moved successfully", response);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    presigned url to download
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getPresignedUrlToDownload (@PathVariable Long fileId, @AuthenticationPrincipal CustomPrincipal member) {
        String presignedUrl = s3Service.getPresignedUrlToDownload(fileId, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Presigned URL generated successfully", presignedUrl);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary = "파일 이름 변경 API")
    @PatchMapping("/{fileId}/rename")
    public ResponseEntity<?> renameFile(@PathVariable Long fileId, @RequestBody RenameFileReqDto renameFileReqDto, @AuthenticationPrincipal CustomPrincipal member) {
        s3Service.renameFile(fileId, renameFileReqDto.getNewFileName(), member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File renamed successfully", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation (summary = "파일 아이디를 통해 폴더위치 조회 API")
    @GetMapping("/{fileId}/location")
    public ResponseEntity<?> getFileLocation(@PathVariable Long fileId, @AuthenticationPrincipal CustomPrincipal member) {
        FolderLocationResDto response = s3Service.getFileLocation(fileId, member.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File location retrieved successfully", response);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


}
