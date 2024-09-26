package com.example.coconote.api.channel.channel.service;

import com.example.coconote.api.channel.channel.dto.request.ChannelCreateReqDto;
import com.example.coconote.api.channel.channel.dto.request.ChannelUpdateReqDto;
import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.drive.dto.response.FileListDto;
import com.example.coconote.api.drive.dto.response.FolderAllListResDto;
import com.example.coconote.api.drive.dto.response.FolderListDto;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.api.drive.repository.FolderRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
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
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelMemberRepository channelMemberRepository;

    @Transactional
    public ChannelDetailResDto channelCreate(ChannelCreateReqDto dto, String email) {
        Section section = sectionRepository.findById(dto.getSectionId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 섹션입니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        Channel channel = dto.toEntity(section);

        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, section.getWorkspace(), IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("워크스페이스 회원이 존재하지 않습니다."));

        ChannelMember channelMember = ChannelMember.builder()
                .workspaceMember(workspaceMember)
                .channel(channel)
                .channelRole(ChannelRole.MANAGER)
                .build();

        channelMemberRepository.save(channelMember);
        channel.getChannelMembers().add(channelMember);
        workspaceMember.getChannelMembers().add(channelMember);
        channelRepository.save(channel);
        createDefaultFolder(channel);
        ChannelDetailResDto resDto = channel.fromEntity(section);


        return resDto;
    }

    private void createDefaultFolder(Channel channel) {
        Folder rootFolder = Folder.builder()
                .folderName("root")
                .channel(channel)
                .build();
        Folder folder = Folder.builder()
                .folderName("캔버스 자동업로드 폴더")
                .channel(channel)
                .parentFolder(rootFolder)
                .build();
        Folder folder2 = Folder.builder()
                .folderName("쓰레드 자동업로드 폴더")
                .channel(channel)
                .parentFolder(rootFolder)
                .build();
        folderRepository.save(rootFolder);
        folderRepository.save(folder);
        folderRepository.save(folder2);
    }

    public List<ChannelDetailResDto> channelList(Long sectionId) {

        Section section = sectionRepository.findById(sectionId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 섹션입니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        List<Channel> channels = channelRepository.findBySectionAndIsDeleted(section, IsDeleted.N);
        List<ChannelDetailResDto> dtos = new ArrayList<>();
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
