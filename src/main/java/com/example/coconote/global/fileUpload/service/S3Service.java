package com.example.coconote.global.fileUpload.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.global.fileUpload.dto.request.FileMetadataReqDto;
import com.example.coconote.global.fileUpload.dto.request.FileSaveListDto;
import com.example.coconote.global.fileUpload.dto.request.FileUploadRequest;
import com.example.coconote.global.fileUpload.dto.response.FileMetadataResDto;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "msi", "dll", "vbs"
    );

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final FileRepository fileRepository;
    private final ChannelRepository channelRepository;
    private final FolderRepository folderRepository;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    // 다중 파일에 대한 Presigned URL 생성
    public Map<String, String> generatePresignedUrls(List<FileUploadRequest> files) {
        return files.stream().collect(Collectors.toMap(
                FileUploadRequest::getFileName,
                this::generatePresignedUrlAfterValidation
        ));
    }

    private String generatePresignedUrlAfterValidation(FileUploadRequest file) {
        validateFile(file.getFileSize(), file.getFileName());
        String uniqueFileName = generateUniqueFileName(file.getFileName()); // UUID가 포함된 고유한 파일 이름 생성
        return generatePresignedUrl(uniqueFileName);
    }

    private void validateFile(long fileSize, String fileName) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다: " + fileName);
        }
        String fileExtension = getFileExtension(fileName).toLowerCase();
        if (BLOCKED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("이 파일 형식은 업로드할 수 없습니다: " + fileExtension);
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    // 고유한 파일 이름을 생성하는 메서드 (UUID + 파일 확장자, URL이 너무 길어서)
    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";

        // 파일 확장자 추출
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = originalFileName.substring(dotIndex);  // 확장자 포함
        }

        // 파일 이름을 UUID + 확장자로 축약하여 생성
        return uuid + extension;
    }

    // 단일 파일 Presigned URL 생성
    public String generatePresignedUrl(String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)  // UUID가 포함된 고유한 파일 이름 사용
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignPutObjectRequest ->
                presignPutObjectRequest
                        .signatureDuration(Duration.ofMinutes(10)) // 10분 유효
                        .putObjectRequest(putObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    // 파일 메타데이터 저장 (프론트엔드로부터 Presigned URL을 받아 저장)
    @Transactional
    public List<FileMetadataResDto> saveFileMetadata(FileMetadataReqDto fileMetadataDto) {
        // 채널 검증
        if (fileMetadataDto == null) {
            throw new IllegalArgumentException("파일 메타데이터가 필요합니다.");
        }

        if (fileMetadataDto.getChannelId() == null) {
            throw new IllegalArgumentException("채널 ID가 필요합니다.");
        }

        Channel channel = channelRepository.findById(fileMetadataDto.getChannelId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        // 폴더 검증
        Folder folder;
        if (fileMetadataDto.getFolderId() != null) {
            folder = channel.getFolders().stream()
                    .filter(f -> f.getId().equals(fileMetadataDto.getFolderId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        } else {
            // 폴더가 없을 경우 첫 번째 폴더 사용
            folder = channel.getFolders().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        }

        // 파일 엔티티 생성 및 저장
        List<FileEntity> fileEntities = fileMetadataDto.getFileSaveListDto().stream()
                .map(fileSaveListDto -> createFileEntity(fileSaveListDto, folder))
                .collect(Collectors.toList());

        List<FileEntity> savedEntities = fileRepository.saveAll(fileEntities);

        return savedEntities.stream()
                .map(FileMetadataResDto::fromEntity)
                .collect(Collectors.toList());
    }

    // FileSaveListDto에서 FileEntity를 생성
    private FileEntity createFileEntity(FileSaveListDto fileSaveListDto, Folder folder) {
        return FileEntity.builder()
                .fileName(fileSaveListDto.getFileName()) // 원본 파일 이름 저장
                .fileUrl(fileSaveListDto.getFileUrl()) // 프론트에서 전달된 Presigned URL을 데이터베이스에 저장
                .folder(folder) // 폴더 정보 추가
                .build();
    }
}
