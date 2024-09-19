package com.example.coconote.api.drive.service;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.drive.dto.request.CreateFolderReqDto;
import com.example.coconote.api.drive.dto.response.FolderChangeNameResDto;
import com.example.coconote.api.drive.dto.response.FolderCreateResDto;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;


    @Transactional
    public FolderCreateResDto createFolder(CreateFolderReqDto createFolderReqDto, String email) {
        Channel channel = channelRepository.findById(createFolderReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        // 부모 폴더 조회 (parentFolderId가 null이 아닐 경우에만)
        Folder parentFolder = null;
        if (createFolderReqDto.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(createFolderReqDto.getParentFolderId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 폴더가 존재하지 않습니다."));
            if (!parentFolder.getChannel().getId().equals(channel.getId())) {
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


}
