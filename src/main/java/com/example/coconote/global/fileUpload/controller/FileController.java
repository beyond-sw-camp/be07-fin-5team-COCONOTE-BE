package com.example.coconote.global.fileUpload.controller;

import com.example.coconote.common.CommonResDto;
import com.example.coconote.global.fileUpload.dto.request.*;
import com.example.coconote.global.fileUpload.dto.response.FileMetadataResDto;
import com.example.coconote.global.fileUpload.dto.response.MoveFileResDto;
import com.example.coconote.global.fileUpload.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/presigned-urls")
    public ResponseEntity<?> generatePresignedUrls(@RequestBody List<FileUploadRequest> files) {
        Map<String, String> presignedUrls = s3Service.generatePresignedUrls(files);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Presigned URLs generated successfully", presignedUrls);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 2. 파일 메타데이터 저장 API
    @Operation(summary = "파일 URL 저장 API, Channel ID는 필수, Folder ID는 선택(없으면 null 로 보내면 됨)")
    @PostMapping("/metadata")
    public ResponseEntity<?> saveFileMetadata(@RequestBody FileMetadataReqDto fileMetadataList, @RequestParam String email) {
        List<FileMetadataResDto> savedMetadata = s3Service.saveFileMetadata(fileMetadataList, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "File metadata saved successfully", savedMetadata);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    // 3. 파일 삭제 API
    @Operation(summary = "파일 삭제 API (삭제 가능한 사람은 파일을 업로드한 사람 또는 채널의 관리자)")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, String email) {
        s3Service.deleteFile(fileId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File deleted successfully", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    // 4. 파일 이동 API
    @PatchMapping
    public ResponseEntity<?> moveFile(@RequestBody MoveFileReqDto moveFileReqDto, String email) {
        MoveFileResDto response = s3Service.moveFile(moveFileReqDto, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "File moved successfully", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    presigned url to download
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getPresignedUrlToDownload (@PathVariable Long fileId,@RequestParam String email) {
        String presignedUrl = s3Service.getPresignedUrlToDownload(fileId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Presigned URL generated successfully", presignedUrl);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


}
