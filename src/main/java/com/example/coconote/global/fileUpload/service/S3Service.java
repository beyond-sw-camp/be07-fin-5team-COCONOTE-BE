package com.example.coconote.global.fileUpload.service;

import com.example.coconote.global.fileUpload.dto.response.PresignedUrlResDto;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "msi", "dll", "vbs"
    );

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

//      주어진 파일 이름으로 S3에 업로드하기 위한 프리사인드 URL을 생성합니다.
    @Transactional
    public PresignedUrlResDto generatePresignedUrl(String fileName) {
        validateFileName(fileName);

        String key = generateUniqueFileKey(fileName);
        String presignedUrl = createPresignedUploadUrl(key);
        String fileUrl = buildS3FileUrl(key);

        saveFileMetadata(fileName, key, fileUrl);

        return new PresignedUrlResDto(presignedUrl, key, fileUrl);
    }

//      주어진 파일 키로 S3에서 파일을 삭제하고, 데이터베이스에서 제거
    @Transactional
    public void deleteFile(String key) {
        deleteFileFromS3(key);
        deleteFileMetadata(key);
    }


//
//
//    함수들
    private void validateFileName(String fileName) {
        if (isBlockedExtension(fileName) || fileName.contains("..")) {
            throw new IllegalArgumentException("유효하지 않은 파일 형식입니다.");
        }
    }

    private boolean isBlockedExtension(String fileName) {
        String extension = getFileExtension(fileName);
        return BLOCKED_EXTENSIONS.contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex != -1 && lastDotIndex != fileName.length() - 1)
                ? fileName.substring(lastDotIndex + 1)
                : "";
    }

    private String generateUniqueFileKey(String fileName) {
        return UUID.randomUUID() + "-" + fileName;
    }

    private String createPresignedUploadUrl(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    private String buildS3FileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private void saveFileMetadata(String fileName, String key, String fileUrl) {
        FileEntity fileEntity = FileEntity.builder()
                .fileName(fileName)
                .s3Key(key)
                .fileUrl(fileUrl)
                .build();
        fileRepository.save(fileEntity);
    }

    private void deleteFileFromS3(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    private void deleteFileMetadata(String key) {
        FileEntity fileEntity = fileRepository.findByS3Key(key)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
        fileRepository.delete(fileEntity);
    }
}