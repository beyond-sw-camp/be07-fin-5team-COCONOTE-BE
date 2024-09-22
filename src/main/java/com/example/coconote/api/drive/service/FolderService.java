package com.example.coconote.api.drive.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.drive.dto.request.CreateFolderReqDto;
import com.example.coconote.api.drive.dto.response.*;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;


    @Transactional
    public FolderCreateResDto createFolder(CreateFolderReqDto createFolderReqDto, String email) {
        Channel channel = channelRepository.findById(createFolderReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        // 부모 폴더 조회 (parentFolderId가 null이 아닐 경우에만)
        Folder parentFolder = null;
        if (createFolderReqDto.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(createFolderReqDto.getParentFolderId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 폴더가 존재하지 않습니다."));
            if (!parentFolder.getChannel().getChannelId().equals(channel.getChannelId())) {
                throw new IllegalArgumentException("부모 폴더가 다른 채널에 있습니다.");
            }
        }
        Folder folder = Folder.builder()
                .folderName("새폴더")
                .channel(channel)
                .parentFolder(parentFolder)
                .build();
        folderRepository.save(folder);
        return FolderCreateResDto.fromEntity(folder);
    }

    @Transactional
    public FolderChangeNameResDto updateFolderName(Long folderId, String folderName, String email) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
        folder.changeFolderName(folderName);
        return FolderChangeNameResDto.fromEntity(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId, String email) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
        folder.markAsDeleted(); // 실제 삭제 대신 소프트 삭제 처리
    }


    @Transactional
    public MoveFolderResDto moveFolder(Long folderId, Long parentId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
        Folder parentFolder = folderRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("부모 폴더가 존재하지 않습니다."));
        if (!folder.getChannel().getChannelId().equals(parentFolder.getChannel().getChannelId())) {
            throw new IllegalArgumentException("폴더가 다른 채널에 있습니다.");
        }
//        todo  바꾸려는 유저가 채널에 속해있는지 확인

        folder.moveParentFolder(parentFolder);
        return MoveFolderResDto.builder()
                .folderId(folder.getId())
                .parentId(folder.getParentFolder().getId())
                .folderName(folder.getFolderName())
                .channelId(folder.getChannel().getChannelId())
                .build();
    }

    public FolderAllListResDto getAllFolderList(Long folderId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
        List<Folder> folderList = folderRepository.findAllByParentFolderAndIsDeleted(folder, IsDeleted.N);
        List<FileEntity> fileEntityList = fileRepository.findAllByFolderAndIsDeleted(folder, IsDeleted.N);


        List<FolderListDto> folderListDto = FolderListDto.fromEntity(folderList);
        List<FileListDto> fileListDto = FileListDto.fromEntity(fileEntityList);

        return FolderAllListResDto.builder()
                .nowFolderId(folder.getId())
                .nowFolderName(folder.getFolderName())
                .folderListDto(folderListDto)
                .fileListDto(fileListDto)
                .build();


    }
}
