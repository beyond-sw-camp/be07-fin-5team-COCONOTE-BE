package com.example.coconote.api.canvas.canvas.controller;

import com.example.coconote.api.canvas.block.service.BlockService;
import com.example.coconote.api.canvas.canvas.dto.request.CanvasSocketReqDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.entity.CanvasMessageMethod;
import com.example.coconote.api.canvas.canvas.entity.PostMessageType;
import com.example.coconote.api.canvas.canvas.service.CanvasService;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import com.example.coconote.security.token.JwtTokenProvider;
import com.example.coconote.security.util.CustomPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Controller
@Slf4j
public class WebsocketController {


    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CanvasService canvasService;
    private final BlockService blockService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * ⭐ 캔버스용
     * websocket "/pub/canvas/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/canvas/message")
    public void message(CanvasSocketReqDto roomMessage, @Header("Authorization") String token) {
        Long id = jwtTokenProvider.getMemberIdFromToken(token);
        roomMessage.setSenderId(id);

        Member member = memberRepository.findById(roomMessage.getSenderId()).orElseThrow(() -> new EntityNotFoundException("해당멤버가 없습니다."));
        Workspace workspace = workspaceRepository.findById(roomMessage.getWorkspaceId()).orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스가 없습니다."));
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스 멤버가 없습니다."));

        roomMessage.setWorkspaceMemberId(workspaceMember.getWorkspaceMemberId());

        kafkaTemplate.send("canvas-topic", roomMessage);
        if(roomMessage.getPostMessageType().equals(PostMessageType.CANVAS)){
            canvasService.editCanvasInSocket(roomMessage, workspaceMember);
        }else if(roomMessage.getPostMessageType().equals(PostMessageType.BLOCK)){
            blockService.editBlockInSocket(roomMessage, workspaceMember);
        }
//        log.info("카프카 이전 message : " + message.toString());

    }

    /**
     * 캔버스 내부 block용
     * websocket "/pub/block/message"로 들어오는 메시징을 처리한다.
     * ⭐ pub/canvas/message로 모든 주소 통일 ⭐
     */
//    @MessageMapping("/block/message")
//    public void messageBlock(CanvasSocketReqDto message) {
//        if (CanvasSocketReqDto.MessageType.ENTER.equals(message.getType())) {
////            canvasService.enterChatRoom(message.getRoomId()); // room에 접속
////            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
//        }else{
//            kafkaTemplate.send("block-topic", message);
//        }
//    }

    // 모든 채팅방 목록 반환
    @GetMapping("/canvas/rooms")
    @ResponseBody
    public List<CanvasListResDto> room() {
        return canvasService.findAllRoom();
    }

}