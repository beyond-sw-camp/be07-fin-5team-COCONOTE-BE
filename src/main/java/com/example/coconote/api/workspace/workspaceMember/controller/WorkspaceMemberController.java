package com.example.coconote.api.workspace.workspaceMember.controller;

import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.api.workspace.workspaceMember.service.WorkspaceMemberService;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberUpdateReqDto;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;
    @Autowired
    public WorkspaceMemberController(WorkspaceMemberService workspaceMemberService) {
        this.workspaceMemberService = workspaceMemberService;
    }

    @Operation(summary= "웤스 회원 가입")
    @PostMapping("/workspace/{workspaceId}/member/create")
    public ResponseEntity<Object> workspaceMemberCreate(@PathVariable Long workspaceId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        WorkspaceMemberResDto resDto = workspaceMemberService.workspaceMemberCreate(workspaceId, customPrincipal.getEmail() );
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(summary= "웤스 회원 목록 조회")
    @GetMapping("/workspace/{workspaceId}/member/list")
    public ResponseEntity<Object> workspaceMemberRead(@PathVariable Long workspaceId) {
        List<WorkspaceMemberResDto> dtos = workspaceMemberService.workspaceMemberList(workspaceId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "웤스 회원 상세 조회")
    @GetMapping("/workspace/member/{workspaceMemberId}")
    public ResponseEntity<Object> workspaceMemberDetail(@PathVariable Long workspaceMemberId) {
        WorkspaceMemberResDto dto = workspaceMemberService.workspaceMemberDetail(workspaceMemberId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is successfully found", dto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



    @Operation(summary= "웤스 회원 정보 수정")
    @PatchMapping("/workspace/member/update/{id}")
    public ResponseEntity<Object> workspaceMemberUpdate(@PathVariable Long id, @RequestBody WorkspaceMemberUpdateReqDto dto) {
        WorkspaceMemberResDto resDto = workspaceMemberService.workspaceMemberUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is successfully updated", resDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @Operation(summary= "웤스 회원 관리자 권한 부여/삭제")
    @PatchMapping("/workspace/member/changerole/{id}")
    public ResponseEntity<Object> workspaceMemberChangeRole(@PathVariable Long id) {
        CommonResDto commonResDto;
        Boolean value = workspaceMemberService.workspaceMemberChangeRole(id);
        if(value) {
            commonResDto = new CommonResDto(HttpStatus.OK, "role is successfully changed to sManager", value);
        }else{
            commonResDto = new CommonResDto(HttpStatus.OK, "role is successfully changed to user", value);
        }
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(summary= "웤스 회원 강퇴/탈퇴")
    @DeleteMapping("/workspace/member/delete/{id}")
    public ResponseEntity<Object> workspaceDelete(@PathVariable Long id) {
        workspaceMemberService.workspaceMemberDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is successfully deleted", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



}
