package com.example.coconote.global.fileUpload.controller;

import com.example.coconote.global.fileUpload.dto.response.PresignedUrlResDto;
import com.example.coconote.global.fileUpload.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<PresignedUrlResDto> getUploadUrl(@RequestParam("fileName") String fileName) {
        PresignedUrlResDto response = s3Service.generatePresignedUrl(fileName);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully");
    }
}
