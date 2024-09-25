package com.example.coconote.api.drive.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.drive.dto.request.CreateFolderReqDto;
import com.example.coconote.api.drive.dto.response.*;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ChannelMemberRepository channelMemberRepository;


    @Transactional
    public FolderCreateResDto createFolder(CreateFolderReqDto createFolderReqDto, String email) {
        Channel channel = getChannelByChannelId(createFolderReqDto.getChannelId());
        Member member =  getMemberByEmail(email);
        Workspace workspace = getWorkspaceByChannel(channel);
//        채널 멤버인지 확인
        checkChannelMember(member, channel);
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
        Member member = getMemberByEmail(email);
        Folder folder = getFolderByFolderId(folderId);
        Channel channel = getChannelByChannelId(folder.getChannel().getChannelId());
//        채널 멤버인지 확인
        checkChannelMember(member, channel);

        folder.changeFolderName(folderName);
        return FolderChangeNameResDto.fromEntity(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId, String email) {
        Member member = getMemberByEmail(email);
        Folder folder = getFolderByFolderId(folderId);
        Channel channel = getChannelByChannelId(folder.getChannel().getChannelId());
//        채널 멤버인지 확인
        checkChannelMember(member, channel);
//        자식 폴더들도 재귀적으로 삭제 처리
        folderRepository.softDeleteChildFolders(IsDeleted.Y, LocalDateTime.now(), folder);
        fileRepository.softDeleteFilesInFolder(IsDeleted.Y, LocalDateTime.now(), folder);
        folder.markAsDeleted(); // 실제 삭제 대신 소프트 삭제 처리 자신 삭제
    }


    @Transactional
    public MoveFolderResDto moveFolder(Long folderId, Long parentId, String email) {
        Member member = getMemberByEmail(email);
        Folder folder = getFolderByFolderId(folderId);
        Folder parentFolder = getFolderByFolderId(parentId);
        Channel channel = getChannelByChannelId(folder.getChannel().getChannelId());
//       폴더 두개가 같은 채널에 있는지 확인
        Channel parentChannel = getChannelByChannelId(parentFolder.getChannel().getChannelId());
        if (!channel.getChannelId().equals(parentChannel.getChannelId())) {
            throw new IllegalArgumentException("폴더가 서로 다른 채널에 있습니다.");
        }
//        채널 멤버인지 확인
        checkChannelMember(member, parentFolder.getChannel());

        if (!folder.getChannel().getChannelId().equals(parentFolder.getChannel().getChannelId())) {
            throw new IllegalArgumentException("폴더가 다른 채널에 있습니다.");
        }

        folder.moveParentFolder(parentFolder);
        return MoveFolderResDto.builder()
                .folderId(folder.getId())
                .parentId(folder.getParentFolder().getId())
                .folderName(folder.getFolderName())
                .channelId(folder.getChannel().getChannelId())
                .build();
    }

    public FolderAllListResDto getAllFolderList(Long folderId, String email) {
        Member member = getMemberByEmail(email);
        Folder folder = getFolderByFolderId(folderId);
//        채널이 공개 채널인지 확인
        Channel channel = getChannelByChannelId(folder.getChannel().getChannelId());
//        체날 멤버인지 확인

        if (!channel.getIsPublic()) {
            throw new IllegalArgumentException("비공개 채널입니다.");
        }

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

    private Channel getChannelByChannelId(Long channelId) {
        return channelRepository.findById(channelId).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    private Workspace getWorkspaceByChannel(Channel channel) {
        return workspaceRepository.findById(channel.getSection().getWorkspace().getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스가 존재하지 않습니다."));
    }
    private Folder getFolderByFolderId(Long folderId) {
        return folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
    }

//    채널 멤버인지 확인
    private void checkChannelMember(Member member, Channel channel) {
        if (!channel.getChannelMembers().contains(member)) {
            throw new IllegalArgumentException("채널 멤버가 아닙니다.");
        }
    }




}
