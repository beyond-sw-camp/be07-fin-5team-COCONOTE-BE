package com.example.coconote.api.canvas.canvas.service;

import com.example.coconote.api.canvas.block.service.BlockService;
import com.example.coconote.api.canvas.canvas.dto.request.*;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.entity.CanvasMessageMethod;
import com.example.coconote.api.canvas.canvas.entity.PostMessageType;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.CanvasBlockDocument;
import com.example.coconote.api.search.mapper.CanvasBlockMapper;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CanvasService {
    private final CanvasRepository canvasRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final SearchService searchService;
    private final CanvasBlockMapper canvasBlockMapper;
    private final BlockService blockService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    public CanvasService(CanvasRepository canvasRepository, ChannelRepository channelRepository, MemberRepository memberRepository, KafkaTemplate<String, Object> kafkaTemplate, SimpMessageSendingOperations messagingTemplate, SearchService searchService, BlockService blockService, CanvasBlockMapper canvasBlockMapper, WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository){
        this.canvasRepository = canvasRepository;
        this.channelRepository = channelRepository;
        this.memberRepository = memberRepository;

//        websocket 용도
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
        this.searchService = searchService;
        this.canvasBlockMapper = canvasBlockMapper;
        this.blockService = blockService;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public CreateCanvasResDto createCanvas(CanvasSocketReqDto createCanvasReqDto, WorkspaceMember workspaceMember) {
        Channel channel = channelRepository.findById(createCanvasReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Canvas parentCanvas = null;
        // 부모 캔버스 조회 (parentCanvasId가 null이 아닐 경우에만)
        if (createCanvasReqDto.getParentCanvasId() != null) {
            parentCanvas = canvasRepository.findById(createCanvasReqDto.getParentCanvasId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 캔버스가 존재하지 않습니다."));

            // 부모 캔버스가 현재 채널에 속해 있는지 확인
            if (!parentCanvas.getChannel().getChannelId().equals(channel.getChannelId())) {
                throw new IllegalArgumentException("부모 캔버스가 현재 채널에 속해 있지 않습니다.");
            }
        }

        Canvas prevCanvas = null;
        if (createCanvasReqDto.getPrevCanvasId() != null) {
            prevCanvas = canvasRepository.findByIdAndIsDeleted(createCanvasReqDto.getPrevCanvasId(), IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("해당 이전 Block이 존재하지 않습니다."));
        }

        Canvas canvas = Canvas.builder()
                .title(createCanvasReqDto.getCanvasTitle())
                .parentCanvas(parentCanvas)
                .prevCanvas(prevCanvas)
                .channel(channel)
                .workspaceMember(workspaceMember)
                .build();

        canvasRepository.save(canvas);
//        검색 인덱스에 저장
        CanvasBlockDocument document = canvasBlockMapper.toDocument(canvas);
        IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(channel.getSection().getWorkspace().getWorkspaceId() , EntityType.CANVAS, document);
        kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());

        topics.put(canvas.getId(), canvas.getId());

        return CreateCanvasResDto.fromEntity(canvas);
    }

    public Page<CanvasListResDto> getCanvasListInChannel(Long channelId, String email, Pageable pageable, Integer depth) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Page<Canvas> canvasList = canvasRepository.findByChannelAndParentCanvasIdAndIsDeleted(pageable, channel, null, IsDeleted.N);


//        List<CanvasListResDto> childCanvas = null;
//        if(depth > 0){
//            for (int i = 0; i<depth; i++){
//                Canvas paerntCanvas = null;
//            }
//        }

        Page<CanvasListResDto> canvasListResDtos = canvasList.map(a -> a.fromListEntity());
        return canvasListResDtos;
    }

    //    현 캔버스를 참조하고 있는 하위 캔버스
    public List<CanvasListResDto> getChildCanvasListFromCanvas(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        List<Canvas> childCanvas = canvasRepository.findByParentCanvasIdAndIsDeleted(canvas.getId(), IsDeleted.N);
        List<CanvasListResDto> childCanvasListDto = !childCanvas.isEmpty() ?
                childCanvas.stream().map(a -> a.fromListEntity()).toList()
                : null;

        return childCanvasListDto;
    }

    //    현 캔버스와 형제 캔버스
    public List<CanvasListResDto> getChildCanvasListFromParentCanvas(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        Canvas parentCanvas = null;
        if (canvas.getParentCanvas() != null) {
            parentCanvas = canvasRepository.findByIdAndIsDeleted(canvas.getParentCanvas().getId(), IsDeleted.N).orElse(null);
        }

        List<Canvas> siblingCanvasList = canvasRepository.findByParentCanvasIdAndChannelAndIsDeleted(parentCanvas != null ? parentCanvas.getId() : null,
                canvas.getChannel(), IsDeleted.N);
        List<CanvasListResDto> siblingCanvasListDto = !siblingCanvasList.isEmpty() ?
                siblingCanvasList.stream().map(a -> a.fromListEntity()).toList()
                : null;


        return siblingCanvasListDto;
    }

    @Transactional
    public CanvasDetResDto getCanvasDetail(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        CanvasDetResDto canvasDetResDto = canvas.fromDetEntity();
        return canvasDetResDto;
    }

    public CanvasDetResDto updateCanvas(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getCanvasId(), IsDeleted.N)
                .orElseThrow(() -> new EntityNotFoundException("캔버스가 존재하지 않습니다."));
        Canvas parentCanvas = null;
        if (canvasSocketReqDto.getParentCanvasId() != null) {
            parentCanvas = canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getParentCanvasId(), IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("해당 부모 캔버스가 존재하지 않습니다."));
        }

        Canvas prevCanvas = canvasSocketReqDto.getPrevCanvasId() != null
                ? canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getPrevCanvasId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Prev Canvas이 존재하지 않습니다."))
                : null;

        //        prev canvas 존재 및 이전에 해당 prev canvas을 갖고있는 canvas 주소 업데이트
        if (prevCanvas != null) {
            Canvas originalPrevCanvasHolder = canvasRepository.findByPrevCanvas_IdAndIsDeleted(prevCanvas.getId(), IsDeleted.N)
                    .orElse(null);
            if (originalPrevCanvasHolder != null) {
                originalPrevCanvasHolder.changePrevCanvas(canvas);
            }
        }

        canvas.updateInfo(canvasSocketReqDto.getCanvasTitle(), parentCanvas, IsDeleted.N);
        return canvas.fromDetEntity();
    }

    private boolean changeOrderCanvas(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
        log.info("순서 변경!! canvasSocketReqDto {}", canvasSocketReqDto);

        // 1. id로 현재 캔버스 찾기
        Canvas currentCanvas = canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getCanvasId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Canvas이 존재하지 않습니다."));

        // 2. 새로운 prevCanvasId와 nextCanvasId로 캔버스 찾기 (없으면 null 허용)
        Canvas newPrevCanvas = null;
        if (canvasSocketReqDto.getPrevCanvasId() != null) {
            newPrevCanvas = canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getPrevCanvasId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 앞 캔버스일 수 있음)
        }

        Canvas newNextCanvas = null;
        if (canvasSocketReqDto.getNextCanvasId() != null) {
            newNextCanvas = canvasRepository.findByIdAndIsDeleted(canvasSocketReqDto.getNextCanvasId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 뒤 캔버스일 수 있음)
        }

        // 3. 기존 prevCanvas과 nextCanvas 연결 끊기
        Canvas originalPrevCanvas = currentCanvas.getPrevCanvas();
        Canvas originalNextCanvas = canvasRepository.findByPrevCanvas_IdAndIsDeleted(currentCanvas.getId(), IsDeleted.N)
                .orElse(null);

        // 기존 prevCanvas이 연결한 nextCanvas을 업데이트
        if (originalNextCanvas != null) {
            originalNextCanvas.changePrevCanvas(originalPrevCanvas);
            canvasRepository.save(originalNextCanvas);
            CanvasBlockDocument canvasBlockDocument = originalNextCanvas.fromBlockDocEntity();
            searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), canvasBlockDocument);

        }

        // 4. 새로운 prevCanvas과의 연결 설정
        if (newPrevCanvas != null) {
            Canvas nextOfNewPrevCanvas = canvasRepository.findByPrevCanvas_IdAndIsDeleted(newPrevCanvas.getId(), IsDeleted.N)
                    .orElse(null);

            // 새 prevCanvas이 가지고 있던 nextCanvas의 prevCanvas을 currentCanvas으로 설정
            if (nextOfNewPrevCanvas != null && !nextOfNewPrevCanvas.equals(currentCanvas)) {
                nextOfNewPrevCanvas.changePrevCanvas(currentCanvas);
                canvasRepository.save(nextOfNewPrevCanvas);
                CanvasBlockDocument canvasBlockDocument = nextOfNewPrevCanvas.fromBlockDocEntity();
                searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), canvasBlockDocument);

            }

            // 현재 캔버스의 prevCanvas을 새로운 prevCanvas으로 설정
            currentCanvas.changePrevCanvas(newPrevCanvas);
        } else {
            // 새 prevCanvas이 없다면, 현재 캔버스을 첫 번째 캔버스으로 만듭니다.
            currentCanvas.changePrevCanvas(null);
        }

        // 5. 새로운 nextCanvas과의 연결 설정
        if (newNextCanvas != null) {
            newNextCanvas.changePrevCanvas(currentCanvas);
            canvasRepository.save(newNextCanvas);
            CanvasBlockDocument canvasBlockDocument = newNextCanvas.fromBlockDocEntity();
            searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), canvasBlockDocument);

        }

        // 6. 현재 캔버스을 저장하여 순서 변경 적용
        canvasRepository.save(currentCanvas);
        CanvasBlockDocument canvasBlockDocument = currentCanvas.fromBlockDocEntity();
        searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), canvasBlockDocument);
        log.info("캔버스 순서가 성공적으로 변경되었습니다.");
        return true;
    }

    public void deleteCanvas(Long canvasId, WorkspaceMember workspaceMember) {
        Canvas canvas = canvasRepository.findById(canvasId)
                .orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        Canvas prevLinkedCanvas = canvasRepository.findByPrevCanvas_IdAndIsDeleted(canvas.getId(), IsDeleted.N)
                .orElse(null);


        // 삭제하는 canvas가 참조하고 있던 canvas의 prev 값을 현 삭제 canvas의 prev 값으로 수정
        if (prevLinkedCanvas != null) {
            Canvas canvasPrevOriginBlock = null;
            if(canvas.getPrevCanvas() != null){ // 삭제하는 기존 캔버스의 prev canvas 값
//                canvas.getPrevCanvas()가 자동으로 호출되지 않는 문제로 따로 변수에 호출해서 담음
                canvasPrevOriginBlock = canvasRepository.findByIdAndIsDeleted(canvas.getPrevCanvas().getId(), IsDeleted.N).orElse(null);
            }
            prevLinkedCanvas.changePrevCanvas(canvasPrevOriginBlock);
        }
        canvas.markAsDeleted(); // 실제 삭제 대신 소프트 삭제 처리
        searchService.deleteCanvas(canvas.getChannel().getSection().getWorkspace().getWorkspaceId(), canvas.getId());
    }


//    ========== 기능 불러와서 쓰기~

    public Canvas findByIdAndIsDeletedReturnRequired(Long canvasId) {
        return canvasRepository.findById(canvasId).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
    }

    public Canvas findByIdAndIsDeletedReturnOrElseNull(Long canvasId) {
        return canvasRepository.findById(canvasId).orElse(null);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }


    //    ========== websocket 소스코드 영역
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private Map<Long, Long> topics;

    @PostConstruct
    private void init() {
        topics = new HashMap<>();
    }

    public List<CanvasListResDto> findAllRoom() {
        // 실제 구현 필요
        return null;
    }

    public CanvasDetResDto findRoomById(String id) {
        // 실제 구현 필요
        return null;
    }

//    public CanvasDetResDto createChatRoom(String name) {
//        CanvasDetResDto chatRoom = CanvasDetResDto.create(name);
//        topics.put(chatRoom.getId(), chatRoom.getId());
//        return chatRoom;
//    }

    public void enterChatRoom(Long canvasId) {
        Long topic = topics.get(canvasId);
        if (topic == null) {
            topics.put(canvasId, canvasId);
        }
    }

    public Long getTopic(Long canvasId) {
        return topics.get(canvasId);
    }

//    @KafkaListener(topics = "#{canvasService.getTopic(#roomId)}", groupId = "chat-group")
//    public void listenToMessages(String message) {
//        // 메시지 처리 로직 추가
//        System.out.println("Received Message: " + message);
//    }

    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    @KafkaListener(topics = "canvas-topic")
    public void consumerProductQuantity(String message) { // return 시, string 형식으로 message가 들어옴
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
//            System.out.println("Kafka 이후 메세지"+ message + "/" + message.toString());
            // ChatMessage 객채로 맵핑
            CanvasSocketReqDto roomMessage = objectMapper.readValue(message, CanvasSocketReqDto.class);

            Member member = memberRepository.findById(roomMessage.getSenderId()).orElseThrow(() -> new EntityNotFoundException("해당멤버가 없습니다."));
            Workspace workspace = workspaceRepository.findById(roomMessage.getWorkspaceId()).orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스가 없습니다."));
            WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스 멤버가 없습니다."));

            roomMessage.setWorkspaceMemberId(workspaceMember.getWorkspaceMemberId());

            messagingTemplate.convertAndSend("/sub/canvas/room/" + roomMessage.getChannelId(), roomMessage);
//            SendCanvasReqDto sendCanvasReqDto = objectMapper.readValue(roomMessage.getMessage(), SendCanvasReqDto.class);
            if(roomMessage.getPostMessageType().equals(PostMessageType.CANVAS)){
                editCanvasInSocket(roomMessage, workspaceMember);
            }else if(roomMessage.getPostMessageType().equals(PostMessageType.BLOCK)){
                blockService.editBlockInSocket(roomMessage, workspaceMember);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 처리 오류: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("알 수 없는 오류: {}", e.getMessage());
            throw e;  // 예외를 다시 던져 트랜잭션을 롤백하도록 함
        }
        System.out.println(message);
    }

    public void editCanvasInSocket(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
//        생성, 수정, 삭제인지 type 구분해서 넣어주는 용도
        if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CREATE_CANVAS)) { // 생성 캔버스
            createCanvas(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.UPDATE_CANVAS)) { // 수정 캔버스
            updateCanvas(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CHANGE_ORDER_CANVAS)) { //순서 변경  캔버스
            changeOrderCanvas(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.DELETE_CANVAS)) { // 삭제 캔버스
            deleteCanvas(canvasSocketReqDto.getCanvasId(), workspaceMember);
        } else {
            log.error("잘못된 canvas method");
        }
    }

}
