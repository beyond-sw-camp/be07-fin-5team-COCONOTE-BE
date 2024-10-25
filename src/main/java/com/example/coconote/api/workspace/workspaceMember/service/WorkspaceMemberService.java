package com.example.coconote.api.workspace.workspaceMember.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberRoleReqDto;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberUpdateReqDto;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.security.util.CustomPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.jdbc.Work;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final OpenSearchClient openSearchClient;  // OpenSearchClient 의존성 주입
    private final SearchService searchService;
    private final KafkaTemplate<String, Object> kafkaTemplate;



    @Transactional
    public WorkspaceMemberResDto workspaceMemberCreate(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }

        Member member = getMemberByEmail(email);
        if(member.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        // 이미 워크스페이스 회원일 때 예외
        if(workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).isPresent()) {
            throw new IllegalArgumentException("이미 워크스페이스에 가입되어 있는 회원입니다.");
        }
        if(workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.Y).isPresent()) {
            WorkspaceMember workspaceMemberCameBack = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.Y).orElseThrow(()-> new EntityNotFoundException("없는 회원입니다."));
            workspaceMemberCameBack.restoreEntity();
            return workspaceMemberCameBack.fromEntity();
        }

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .member(member)
                .nickname(member.getNickname())
                .memberName(member.getNickname())
                .build();
        workspaceMemberRepository.save(workspaceMember);

// OpenSearch에 인덱싱
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        IndexEntityMessage<WorkspaceMemberDocument> indexEntityMessage = new IndexEntityMessage<>(workspace.getWorkspaceId(), EntityType.WORKSPACE_MEMBER , document);
        kafkaTemplate.send("workspace_member_entity_search", indexEntityMessage.toJson());

        return workspaceMember.fromEntity();
    }

    public List<WorkspaceMemberResDto> workspaceMemberList(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("찾을 수 없습니다."));
        if(workspace.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 삭제된 워크스페이스입니다.");
        }
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByWorkspaceAndIsDeleted(workspace, IsDeleted.N);
        List<WorkspaceMemberResDto> dtos = new ArrayList<>();
        for(WorkspaceMember w : workspaceMembers) {
            dtos.add(w.fromEntity());
        }
        return dtos;
    }

    @Transactional
    public WorkspaceMemberResDto workspaceMemberUpdate(Long id, WorkspaceMemberUpdateReqDto dto) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        workspaceMember.updateEntity(dto);

        workspaceMemberRepository.save(workspaceMember);
// OpenSearch에 인덱싱
        WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
        IndexEntityMessage<WorkspaceMemberDocument> indexEntityMessage = new IndexEntityMessage<>(workspaceMember.getWorkspace().getWorkspaceId(), EntityType.WORKSPACE_MEMBER , document);
        kafkaTemplate.send("workspace_member_entity_search", indexEntityMessage.toJson());

        WorkspaceMemberResDto restDto = workspaceMember.fromEntity();
        return restDto;
    }


    @Transactional
    public WorkspaceMemberResDto workspaceMemberChangeRole(WorkspaceMemberRoleReqDto dto, CustomPrincipal customPrincipal) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(dto.getId()).orElseThrow(()->new EntityNotFoundException("대상의 WorkspaceMember 정보를 찾을 수 없습니다."));
        if(workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        if (workspaceMember.getWsRole() == WsRole.PMANAGER || workspaceMember.getWsRole() == WsRole.SMANAGER) {
            throw new IllegalArgumentException("워크스페이스 소유자는 강등할 수 없습니다.");
        }
        Workspace workspace = workspaceMember.getWorkspace();
        Member myMember = memberRepository.findById(customPrincipal.getMemberId()).orElseThrow(() -> new EntityNotFoundException("사용자의 Member 정보를 찾을 수 없습니다."));
        WorkspaceMember myWorkspaceMember = workspaceMemberRepository.findByMemberAndWorkspace(myMember, workspace).orElseThrow(() -> new EntityNotFoundException("사용자의 WorkspaceMember 정보를 찾을 수 없습니다."));
        if (myWorkspaceMember.getWsRole().equals(WsRole.USER)
                || (myWorkspaceMember.getWsRole().equals(WsRole.SMANAGER) && workspaceMember.getWsRole().equals(WsRole.PMANAGER))) {
            throw new IllegalArgumentException("권한 부여/박탈의 권한이 없습니다.");
        }
        if (workspaceMember.equals(myWorkspaceMember)) {
            throw new IllegalArgumentException("권한 부여/박탈의 권한이 없습니다.");
        }
        workspaceMemberRepository.save(workspaceMember);
        // OpenSearch 에 인덱싱
        searchService.deleteWorkspaceMember(workspaceMember.getWorkspace().getWorkspaceId() ,workspaceMember.getWorkspaceMemberId());
        return workspaceMember.changeRole(dto.getWsRole());
    }

    @Transactional
    public void workspaceMemberDelete(Long id, CustomPrincipal customPrincipal) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("찾을 수 없습니다."));
        if (workspaceMember.getIsDeleted().equals(IsDeleted.Y)) {
            throw new IllegalArgumentException("이미 워크스페이스에서 탈퇴한 회원입니다.");
        }
        Member myMember = memberRepository.findById(customPrincipal.getMemberId()).orElseThrow(() -> new EntityNotFoundException("사용자의 Member 정보를 찾을 수 없습니다."));
        Workspace myWorkspace = workspaceMember.getWorkspace();
        WorkspaceMember myWorkspaceMember = workspaceMemberRepository.findByMemberAndWorkspace(myMember, myWorkspace).orElseThrow(() -> new EntityNotFoundException("사용자의 WorkspcaeMember의 정보를 찾을 수 없습니다."));
        if (myWorkspaceMember.getWsRole().equals(WsRole.USER) || (workspaceMember.getWsRole().equals(WsRole.PMANAGER))) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다.");
        }
        if (workspaceMember.equals(myWorkspaceMember)) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다.");
        }

        workspaceMember.deleteEntity();
        // OpenSearch에서 문서 삭제
        //            DeleteResponse deleteResponse = openSearchClient.delete(d -> d
//                    .index("workspace_members")  // 인덱스 이름
//                    .id(String.valueOf(workspaceMember.getWorkspaceMemberId()))  // 삭제할 문서의 ID
        searchService.deleteWorkspaceMember(workspaceMember.getWorkspace().getWorkspaceId(), workspaceMember.getWorkspaceMemberId());

    }

    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
    }

    public WorkspaceMemberResDto workspaceMemberDetail(Long workspaceMemberId) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(workspaceMemberId).orElseThrow(()-> new EntityNotFoundException("회원을 조회할 수 없습니다."));
        return workspaceMember.fromEntity();
    }

    public void workspaceMemberLeave(Long workspaceId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 워크스페이스 회원입니다."));
        if (workspaceMember.getWsRole().equals(WsRole.PMANAGER)) {
            throw new IllegalArgumentException("워크스페이스 소유자는 탈퇴할 수 없습니다.");
        }
        workspaceMember.deleteEntity();
    }
}