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
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
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

import static com.example.coconote.api.drive.service.FolderService.getFolderAllListResDto;

@Service
@Transactional
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final SectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final SearchService searchService;

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

        searchService.indexChannel(section.getWorkspace().getWorkspaceId(), channel);

        createDefaultFolder(channel);
        ChannelDetailResDto resDto = channel.fromEntity(section);


        return resDto;
    }

    public void createDefaultFolder(Channel channel) {
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

    public List<ChannelDetailResDto> channelList(Long sectionId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));

        Section section = sectionRepository.findById(sectionId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 섹션입니다."));
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        List<Channel> channels = channelRepository.findBySectionAndIsDeleted(section, IsDeleted.N);
        List<ChannelDetailResDto> dtos = new ArrayList<>();
        for(Channel c : channels) {
            // 비공개채널이고 내가 채널멤버도 아니면 -> continue
            // 내가 채널멤버인지 아닌지 알아보기 -> email과 channel 정보로
            // email로 멤버 정보를 받아온다
            // channelMembers 탐색 >
            List<ChannelMember> cMembers = c.getChannelMembers();
            for(ChannelMember cm : cMembers) {
                if(c.getIsPublic()  || cm.getWorkspaceMember().getMember().equals(member)) { // 비공개채널이고 내가 채널멤버도 아님 -> continue
                    dtos.add(c.fromEntity(section));
                }
            }
        }
        return dtos;
    }


    @Transactional
    public Channel channelUpdate(Long id, ChannelUpdateReqDto dto, String email) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(!checkChannelAuthorization(id, email)) {
            throw new IllegalArgumentException("채널을 수정할 권한이 없습니다.");
        }
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        channel.updateEntity(dto);

        channelRepository.save(channel);
        searchService.indexChannel(channel.getSection().getWorkspace().getWorkspaceId(), channel);
        return channel;
    }


    @Transactional
    public void channelDelete(Long id, String email) {
        Channel channel = channelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(!checkChannelAuthorization(id, email)) {
            throw new IllegalArgumentException("채널을 삭제할 권한이 없습니다.");
        }
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
        channel.deleteEntity();
        searchService.deleteChannel(channel.getSection().getWorkspace().getWorkspaceId(), channel.getChannelId().toString());
    }

    public FolderAllListResDto channelDrive(Long channelId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채널입니다."));
        if(channel.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }
//        루트 폴더 찾기
        Folder rootFolder = folderRepository.findByChannelAndParentFolderIsNull(channel).orElseThrow(() -> new EntityNotFoundException("찾을 수 없습니다."));

        return getFolderAllListResDto(rootFolder, folderRepository, fileRepository);
    }

    public List<ChannelDetailResDto> bookmarkList(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));

        if (workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<ChannelDetailResDto> bookmarkChannels = new ArrayList<>();
        for (Section s : sections) {
            if (s.getChannels() != null) {
                for (Channel c : s.getChannels()) {
                  ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(c, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
                  if(channelMember.getIsBookmark()){
                      bookmarkChannels.add(c.fromEntity(s));
                  }
                }
            }
        }
        return bookmarkChannels;
    }


    private Boolean checkChannelAuthorization(Long channelId, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Channel channel = channelRepository.findById(channelId).orElseThrow(()->new EntityNotFoundException("채널을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(channel.getSection().getSectionId()).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("채널 회원을 찾을 수 없습니다."));
        return channelMember.getChannelRole().equals(ChannelRole.MANAGER);
    }

    public ChannelDetailResDto channelFirst(Long workspaceId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스멤버입니다."));
        List<ChannelMember> channelMembers = channelMemberRepository.findByWorkspaceMemberAndIsDeleted(workspaceMember, IsDeleted.N);
        if(channelMembers.equals(null)) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }
        Section section = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N).get(0);

        List<Channel> channels = channelRepository.findBySectionAndIsDeleted(section, IsDeleted.N);
        if(channels.equals(null)) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }
        Channel channel = channels.get(0);
        return channel.fromEntity(section);

    }

    public boolean channelIsJoin(Long id, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Channel channel = channelRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("채널을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(channel.getSection().getSectionId()).orElseThrow(()-> new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("워크스페이스멤버를 찾을 수 없습니다."));

//        채널멤버가 존재하면 true, 존재하지 않으면 false
        ChannelMember channelMember = channelMemberRepository.findByChannelAndWorkspaceMemberAndIsDeleted(channel, workspaceMember, IsDeleted.N).orElse(null);
        return channelMember != null;
    }
}
