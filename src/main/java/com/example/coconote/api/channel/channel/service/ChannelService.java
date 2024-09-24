package com.example.coconote.api.channel.channel.service;

import com.example.coconote.api.channel.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelListResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.drive.dto.response.FileListDto;
import com.example.coconote.api.drive.dto.response.FolderAllListResDto;
import com.example.coconote.api.drive.dto.response.FolderListDto;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.global.fileUpload.entity.FileEntity;
import com.example.coconote.global.fileUpload.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final SectionRepository sectionRepository;
    private final FolderRepository folderRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;

    @Transactional
    public ChannelListResDto channelCreate(ChannelCreateReqDto dto) {
        Section section = sectionRepository.findById(dto.getSectionId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 섹션입니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        Channel channel = dto.toEntity(section);
        channelRepository.save(channel);
        createDefaultFolder(channel);
        ChannelListResDto resDto = channel.fromEntity(section);

        return resDto;
    }

    private void createDefaultFolder(Channel channel) {
        Folder rootFolder = Folder.builder()
                .folderName("root")
                .channel(channel)
                .build();
        Folder folder = Folder.builder()
                .folderName("자동업로드 폴더")
                .channel(channel)
                .parentFolder(rootFolder)
                .build();
        folderRepository.save(rootFolder);
        folderRepository.save(folder);
    }

    public List<ChannelListResDto> channelList(Long sectionId) {

        Section section = sectionRepository.findById(sectionId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 섹션입니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        List<Channel> channels = channelRepository.findByIsDeleted(IsDeleted.N);
        List<ChannelListResDto> dtos = new ArrayList<>();
        for(Channel c : channels) {
            dtos.add(c.fromEntity(section));
        }
        return dtos;
    }

    public Channel channelUpdate(Long id, ChannelUpdateReqDto dto) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        channel.updateEntity(dto);
        return channel;
    }

    public void channelDelete(Long id) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        channel.deleteEntity();
    }

    public FolderAllListResDto channelDrive(Long channelId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
//        루트 폴더 찾기
        Folder rootFolder = folderRepository.findByChannelAndParentFolderIsNull(channel).orElseThrow(() -> new EntityNotFoundException("찾을 수 없습니다."));

        List<Folder> folderList = folderRepository.findAllByParentFolderAndIsDeleted(rootFolder, IsDeleted.N);
        List<FileEntity> fileEntityList = fileRepository.findAllByFolderAndIsDeleted(rootFolder, IsDeleted.N);


        List<FolderListDto> folderListDto = FolderListDto.fromEntity(folderList);
        List<FileListDto> fileListDto = FileListDto.fromEntity(fileEntityList);

        return FolderAllListResDto.builder()
                .nowFolderId(rootFolder.getId())
                .nowFolderName(rootFolder.getFolderName())
                .folderListDto(folderListDto)
                .fileListDto(fileListDto)
                .build();
    }
}
