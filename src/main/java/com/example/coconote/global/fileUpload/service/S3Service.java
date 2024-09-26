package com.example.coconote.global.fileUpload.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.dto.request.FileMetadataReqDto;
import com.example.coconote.global.fileUpload.dto.request.FileSaveListDto;
import com.example.coconote.global.fileUpload.dto.request.FileUploadRequest;
import com.example.coconote.global.fileUpload.dto.request.MoveFileReqDto;
import com.example.coconote.global.fileUpload.dto.response.FileMetadataResDto;
import com.example.coconote.global.fileUpload.dto.response.MoveFileResDto;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.entity.FileType;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
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
    private final MemberRepository memberRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    // 다중 파일에 대한 Presigned URL 생성
    public Map<String, String> generatePresignedUrls(List<FileUploadRequest> files, String email) {
        Member member = getMemberByEmail(email);
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
    public List<FileMetadataResDto> saveFileMetadata(FileMetadataReqDto fileMetadataDto, String email) {
//        유저 검증
        Member member = getMemberByEmail(email);

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
        if(fileMetadataDto.getFolderId() ==null) {
            if (fileMetadataDto.getFileType() == FileType.THREAD) {
                folder = folderRepository.findByChannelAndFolderName(channel, "쓰레드 자동업로드 폴더")
                        .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
            } else if (fileMetadataDto.getFileType() == FileType.CANVAS) {
                folder = folderRepository.findByChannelAndFolderName(channel, "캔버스 자동업로드 폴더")
                        .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
            } else {
                folder = null;
            }
        } else {
            folder = getFolderEntityById(fileMetadataDto.getFolderId());
        }
        // 파일 엔티티 생성 및 저장
        List<FileEntity> fileEntities = fileMetadataDto.getFileSaveListDto().stream()
                .map(fileSaveListDto -> createFileEntity(fileSaveListDto, folder, member))
                .collect(Collectors.toList());

        List<FileEntity> savedEntities = fileRepository.saveAll(fileEntities);

        return savedEntities.stream()
                .map(FileMetadataResDto::fromEntity)
                .collect(Collectors.toList());
    }

    // FileSaveListDto에서 FileEntity를 생성
    private FileEntity createFileEntity(FileSaveListDto fileSaveListDto, Folder folder, Member member) {
        return FileEntity.builder()
                .fileName(fileSaveListDto.getFileName()) // 원본 파일 이름 저장
                .fileUrl(fileSaveListDto.getFileUrl()) // 프론트에서 전달된 Presigned URL을 데이터베이스에 저장
                .folder(folder) // 폴더 정보 추가
                .creator(member)
                .build();
    }

    @Transactional
    public void deleteFile(Long fileId, String email) {
        FileEntity fileEntity = getFileEntityById(fileId);
        Member member = getMemberByEmail(email);
        Channel channel = fileEntity.getFolder().getChannel();
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, channel.getSection().getWorkspace(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스 멤버를 찾을 수 없습니다."));
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("채널 멤버를 찾을 수 없습니다."));

        // 파일 삭제 권한 검증
//        채널 매니저 이거나 파일을 업로드한 사람만 삭제 가능
        if(channelMember.getChannelRole() != ChannelRole.MANAGER  || !channel.getChannelMembers().contains(member)){
            throw new IllegalArgumentException("파일을 삭제할 권한이 없습니다.");
        }

        fileEntity.markAsDeleted();

    }

    @Transactional
    public void hardDeleteFileS3(Long fileId ) {
        FileEntity fileEntity = getFileEntityById(fileId);
        try {
            // S3에서 파일 삭제
            s3Client.deleteObject(b -> b.bucket(bucketName).key(fileEntity.getFileUrl().substring(fileEntity.getFileUrl().lastIndexOf('/') + 1)));
            log.info("S3에서 파일 삭제 완료: {}", fileEntity.getFileName());
        } catch (Exception e) {
            throw new RuntimeException("S3에서 파일 삭제 중 오류 발생: " + e.getMessage());
        }
        fileRepository.delete(fileEntity);
        log.info("파일 삭제 완료: {}", fileEntity.getFileName());
    }

    @Transactional
    public MoveFileResDto moveFile(MoveFileReqDto moveFileReqDto, String email) {
        Member member = getMemberByEmail(email);
        FileEntity fileEntity = getFileEntityById(moveFileReqDto.getFileId());
        Folder folder = getFolderEntityById(moveFileReqDto.getFolderId());

        if (!folder.getChannel().getChannelId().equals(fileEntity.getFolder().getChannel().getChannelId())) {
            throw new IllegalArgumentException("다른 채널에 있는 폴더로 이동할수 없습니다.");
        }
        if (!folder.getChannel().getChannelMembers().contains(member)) {
            throw new IllegalArgumentException("파일을 이동할 권한이 없습니다.");
        }


        fileEntity.moveFolder(folder);
        fileRepository.save(fileEntity);

        return MoveFileResDto.builder()
                .fileId(fileEntity.getId())
                .folderId(folder.getId())
                .fileName(fileEntity.getFileName())
                .createMemberName(fileEntity.getCreator().getEmail())
                .channelId(folder.getChannel().getChannelId())
                .build();
    }


    public String getPresignedUrlToDownload(Long fileId, String email) {
        Member member = getMemberByEmail(email);
        FileEntity fileEntity = getFileEntityById(fileId);

//        파일 다운로드 권한 검증
        if (!fileEntity.getFolder().getChannel().getChannelMembers().contains(member)) {
            throw new IllegalArgumentException("파일을 다운로드할 권한이 없습니다.");
        }

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(b -> b.getObjectRequest(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileEntity.getFileUrl().substring(fileEntity.getFileUrl().lastIndexOf('/') + 1))
                .build())
                .signatureDuration(Duration.ofMinutes(1)));

        // Presigned URL 생성
        return presignedRequest.url().toString();
//        // Presigned URL 생성
//        try {
//            URI presignedUrl = s3Presigner.presignGetObject(b -> b.getObjectRequest(getObjectRequest)
//                            .signatureDuration(Duration.ofMinutes(1)))
//                    .url().toURI();
//            return presignedUrl.toString(); // 클라이언트에 반환
//        }catch (Exception e){
//            throw new IllegalArgumentException("Presigned URL 생성에 실패했습니다.");
//        }
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private FileEntity getFileEntityById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }

    private Folder getFolderEntityById(Long folder){
        return folderRepository.findById(folder)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
    }
}
