package com.example.coconote.global.fileUpload.service;

import com.example.coconote.global.fileUpload.dto.response.PresignedUrlResDto;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Set.of("exe", "bat", "cmd", "sh", "msi", "dll", "vbs"));


    public PresignedUrlResDto generatePresignedUrl(String fileName) {
//        파일의 확장자가 실행 파일 확장자인지 확인, 파일 이름에 '..'이 포함되어 있는지 확인
        if (isBlockedExtension(fileName) || fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file type");
        }

//        파일 이름이 중복되지 않도록 UUID를 이용하여 파일 이름 생성
        String key = UUID.randomUUID().toString() + "-" + fileName;
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(b -> b.bucket(bucketName).key(key))
                .build();

//        파일 업로드 presigned URL 생성
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String s3Url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;



//        파일 업로드 URL과 파일 이름을 반환
        FileEntity fileEntity = FileEntity.builder()
                .fileName(fileName)
                .s3Key(key)
                .fileUrl(s3Url)
                .build();
        fileRepository.save(fileEntity);
        return new PresignedUrlResDto(presignedRequest.url().toString(), key, s3Url);
    }

    public void deleteFile(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());

        FileEntity fileEntity = fileRepository.findByS3Key(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        fileRepository.delete(fileEntity);
    }

    private boolean isBlockedExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return false;
        }
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        return BLOCKED_EXTENSIONS.contains(extension);
    }
}
