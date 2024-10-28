package com.example.coconote.api.section.service;

import com.example.coconote.api.channel.channel.dto.response.ChannelDetailResDto;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.section.dto.request.SectionCreateReqDto;
import com.example.coconote.api.section.dto.request.SectionUpdateReqDto;
import com.example.coconote.api.section.dto.response.SectionListResDto;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.entity.SectionType;
import com.example.coconote.api.section.repository.SectionRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SectionService {

    private final SectionRepository sectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final RedisTemplate<String, Object> sectionRedisTemplate;  // RedisTemplate을 통해 캐시 작업을 처리


    @Autowired
    public SectionService(SectionRepository sectionRepository, WorkspaceRepository workspaceRepository,
                          MemberRepository memberRepository, WorkspaceMemberRepository workspaceMemberRepository
                          , @Qualifier("sectionRedisTemplate") RedisTemplate<String, Object> sectionRedisTemplate) {
        this.sectionRepository = sectionRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.sectionRedisTemplate = sectionRedisTemplate;
    }

    private void clearSectionCache(Long workspaceId) {
        String cacheKey = "sectionList:workspaceId:" + workspaceId;
        sectionRedisTemplate.delete(cacheKey);
        log.info("Cache cleared for key: {}", cacheKey);
    }

    public SectionListResDto sectionCreate(SectionCreateReqDto dto, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        if(workspaceMember.getWsRole().equals(WsRole.USER)) {
            throw new IllegalArgumentException("섹션을 생성할 수 있는 권한이 없습니다.");
        }
        Section section = dto.toEntity(workspace);
        sectionRepository.save(section);
        clearSectionCache(section.getWorkspace().getWorkspaceId());

        return section.fromEntity(member);
    }

    public List<SectionListResDto> sectionList(Long workspaceId, String email) {
        String cacheKey = "sectionList:workspaceId:" + workspaceId;

        // Redis 캐시에서 데이터 조회
        List<SectionListResDto> cachedData = (List<SectionListResDto>) sectionRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;  // 캐시에 데이터가 있을 경우 바로 반환
        }

        // 캐시에 데이터가 없는 경우 DB에서 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        if (workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<Section> sections = sectionRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<SectionListResDto> dtos = new ArrayList<>();
        for (Section s : sections) {
            List<ChannelDetailResDto> filteredChannels = s.getChannels().stream()
                    .filter(channel -> channel.getIsDeleted().equals(IsDeleted.N))
                    .map(channel -> channel.fromEntity(s))
                    .collect(Collectors.toList());

            dtos.add(SectionListResDto.builder()
                    .sectionId(s.getSectionId())
                    .sectionName(s.getSectionName())
                    .channelList(filteredChannels)
                    .build());
        }

        // Redis에 캐싱하고 유효 기간 설정 (예: 10분)
        sectionRedisTemplate.opsForValue().set(cacheKey, dtos, 60, TimeUnit.MINUTES);

        return dtos;
    }

    public SectionListResDto sectionUpdate(Long id, SectionUpdateReqDto dto, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        if(!checkWorkspaceAuthorization(id, email)) {
            throw new IllegalArgumentException("섹션을 수정할 수 있는 권한이 없습니다.");
        }
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        section.updateEntity(dto);
        SectionListResDto resDto = section.fromEntity(member);
        clearSectionCache(section.getWorkspace().getWorkspaceId());
        return resDto;
    }


    public void sectionDelete(Long id, String email) {
        Section section = sectionRepository.findById(id).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        if(!checkWorkspaceAuthorization(id, email)) {
            throw new IllegalArgumentException("섹션을 삭제할 수 있는 권한이 없습니다.");
        }
        if(section.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 섹션입니다.");
        }
        if(section.getSectionType().equals(SectionType.DEFAULT)) {
            throw new IllegalArgumentException("기본 섹션은 삭제할 수 없습니다.");
        }
        clearSectionCache(section.getWorkspace().getWorkspaceId());
        section.deleteEntity();
    }

//    public SectionListResDto switchIndex(SectionSwitchReqDto dto, String email) {
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
//        Section section = sectionRepository.findById(dto.getSectionId()).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
//        List<Channel> channels = section.getChannels();
//        Long fromIndex = dto.getFromIndex();
//        Long toIndex = dto.getToIndex();
//
//        if (fromIndex < 0 || fromIndex >= channels.size() || toIndex < 0 || toIndex >= channels.size() || fromIndex == toIndex) {
//            throw new IllegalArgumentException("잘못된 접근입니다.");
//        }
//        Channel channelToMove = channels.get(Math.toIntExact(fromIndex));
//
//        // 이동할 요소를 원래 위치에서 빈 자리를 만든 후 이동
//        if (fromIndex < toIndex) {
//            for (int i = Math.toIntExact(fromIndex); i < toIndex; i++) {
//                channels.set(i, channels.get(i + 1));
//            }
//        } else {
//            for (int i = Math.toIntExact(fromIndex); i > toIndex; i--) {
//                channels.set(i, channels.get(i - 1));
//            }
//        }
//
//        channels.set(Math.toIntExact(toIndex), channelToMove); // 새로운 위치에 요소 추가
//
//        return section.fromEntity(member);
//
//    }

    private Boolean checkWorkspaceAuthorization(Long SectionId, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Section section = sectionRepository.findById(SectionId).orElseThrow(()->new EntityNotFoundException("섹션을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(section.getWorkspace().getWorkspaceId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
        return !workspaceMember.getWsRole().equals(WsRole.USER);
    }

}
