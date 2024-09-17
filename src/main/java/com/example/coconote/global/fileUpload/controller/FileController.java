package com.example.coconote.global.fileUpload.controller;

import com.example.coconote.common.CommonResDto;
import com.example.coconote.global.fileUpload.dto.request.*;
import com.example.coconote.global.fileUpload.dto.response.FileMetadataResDto;
import com.example.coconote.global.fileUpload.service.S3Service;
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
    @PostMapping("/metadata")
    public ResponseEntity<?> saveFileMetadata(@RequestBody List<FileMetadataReqDto> fileMetadataList) {
        List<FileMetadataResDto> savedMetadata = s3Service.saveFileMetadata(fileMetadataList);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "File metadata saved successfully", savedMetadata);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

}
