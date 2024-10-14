package com.example.coconote.api.canvas.canvas.service;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.block.entity.Method;
import com.example.coconote.api.canvas.canvas.dto.request.*;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.repository.ChannelRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.common.IsDeleted;
import com.fasterxml.jackson.core.JsonProcessingException;
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
@Transactional(readOnly = true)
public class CanvasService {
    private final CanvasRepository canvasRepository;
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final SearchService searchService;

    public CanvasService(CanvasRepository canvasRepository, ChannelRepository channelRepository, MemberRepository memberRepository, KafkaTemplate<String, Object> kafkaTemplate, SimpMessageSendingOperations messagingTemplate, SearchService searchService) {
        this.canvasRepository = canvasRepository;
        this.channelRepository = channelRepository;
        this.memberRepository = memberRepository;

//        websocket 용도
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
        this.searchService = searchService;
    }

    @Transactional
    public CreateCanvasResDto createCanvas(CreateCanvasReqDto createCanvasReqDto, String email) {
        Channel channel = channelRepository.findById(createCanvasReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Member member = getMemberByEmail(email);
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
                .title(createCanvasReqDto.getTitle())
                .parentCanvas(parentCanvas)
                .prevCanvas(prevCanvas)
                .channel(channel)
                .createMember(member)
                .build();

        canvasRepository.save(canvas);
//        검색 인덱스에 저장
        searchService.indexCanvas(channel.getSection().getWorkspace().getWorkspaceId(), canvas);

        topics.put(String.valueOf(canvas.getId()), String.valueOf(canvas.getId()));

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

    @Transactional
    public CanvasDetResDto updateCanvas(UpdateCanvasReqDto updateCanvasReqDto) {
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(updateCanvasReqDto.getCanvasId(), IsDeleted.N)
                .orElseThrow(() -> new EntityNotFoundException("캔버스가 존재하지 않습니다."));
        Canvas parentCanvas = null;
        if (updateCanvasReqDto.getParentCanvasId() != null) {
            parentCanvas = canvasRepository.findByIdAndIsDeleted(updateCanvasReqDto.getParentCanvasId(), IsDeleted.N).orElseThrow(() -> new EntityNotFoundException("해당 부모 캔버스가 존재하지 않습니다."));
        }

        Canvas prevCanvas = updateCanvasReqDto.getPrevCanvasId() != null
                ? canvasRepository.findByIdAndIsDeleted(updateCanvasReqDto.getPrevCanvasId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Prev Canvas이 존재하지 않습니다."))
                : null;

        //        prev canvas 존재 및 이전에 해당 prev canvas을 갖고있는 canvas 주소 업데이트
        if (prevCanvas != null) {
            Canvas originalPrevCanvasHolder = canvasRepository.findByPrevCanvasIdAndIsDeleted(prevCanvas.getId(), IsDeleted.N)
                    .orElse(null);
            if (originalPrevCanvasHolder != null) {
                originalPrevCanvasHolder.changePrevCanvas(canvas);
            }
        }

        canvas.updateInfo(updateCanvasReqDto.getTitle(), parentCanvas, updateCanvasReqDto.getIsDeleted());
        return canvas.fromDetEntity();
    }

    @Transactional
    private boolean changeOrderCanvas(ChangeOrderCanvasReqDto changeOrderCanvasReqDto) {
        log.info("순서 변경!! changeOrderCanvasReqDto {}", changeOrderCanvasReqDto);

        // 1. id로 현재 블록 찾기
        Canvas currentCanvas = canvasRepository.findByIdAndIsDeleted(changeOrderCanvasReqDto.getId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Canvas이 존재하지 않습니다."));

        // 2. 새로운 prevCanvasId와 nextCanvasId로 블록 찾기 (없으면 null 허용)
        Canvas newPrevCanvas = null;
        if (changeOrderCanvasReqDto.getPrevCanvasId() != null) {
            newPrevCanvas = canvasRepository.findByIdAndIsDeleted(changeOrderCanvasReqDto.getPrevCanvasId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 앞 블록일 수 있음)
        }

        Canvas newNextCanvas = null;
        if (changeOrderCanvasReqDto.getNextCanvasId() != null) {
            newNextCanvas = canvasRepository.findByIdAndIsDeleted(changeOrderCanvasReqDto.getNextCanvasId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 뒤 블록일 수 있음)
        }

        // 3. 기존 prevCanvas과 nextCanvas 연결 끊기
        Canvas originalPrevCanvas = currentCanvas.getPrevCanvas();
        Canvas originalNextCanvas = canvasRepository.findByPrevCanvasIdAndIsDeleted(currentCanvas.getId(), IsDeleted.N)
                .orElse(null);

        // 기존 prevCanvas이 연결한 nextCanvas을 업데이트
        if (originalNextCanvas != null) {
            originalNextCanvas.changePrevCanvas(originalPrevCanvas);
            canvasRepository.save(originalNextCanvas);
            searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), originalNextCanvas);

        }

        // 4. 새로운 prevCanvas과의 연결 설정
        if (newPrevCanvas != null) {
            Canvas nextOfNewPrevCanvas = canvasRepository.findByPrevCanvasIdAndIsDeleted(newPrevCanvas.getId(), IsDeleted.N)
                    .orElse(null);

            // 새 prevCanvas이 가지고 있던 nextCanvas의 prevCanvas을 currentCanvas으로 설정
            if (nextOfNewPrevCanvas != null && !nextOfNewPrevCanvas.equals(currentCanvas)) {
                nextOfNewPrevCanvas.changePrevCanvas(currentCanvas);
                canvasRepository.save(nextOfNewPrevCanvas);
                searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), nextOfNewPrevCanvas);

            }

            // 현재 블록의 prevCanvas을 새로운 prevCanvas으로 설정
            currentCanvas.changePrevCanvas(newPrevCanvas);
        } else {
            // 새 prevCanvas이 없다면, 현재 블록을 첫 번째 블록으로 만듭니다.
            currentCanvas.changePrevCanvas(null);
        }

        // 5. 새로운 nextCanvas과의 연결 설정
        if (newNextCanvas != null) {
            newNextCanvas.changePrevCanvas(currentCanvas);
            canvasRepository.save(newNextCanvas);
            searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), newNextCanvas);

        }

        // 6. 현재 블록을 저장하여 순서 변경 적용
        canvasRepository.save(currentCanvas);
        searchService.indexCanvas(currentCanvas.getChannel().getSection().getWorkspace().getWorkspaceId(), currentCanvas);
        log.info("블록 순서가 성공적으로 변경되었습니다.");
        return true;
    }

    @Transactional
    public void deleteCanvas(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findById(canvasId)
                .orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        Canvas prevLinkedCanvas = canvasRepository.findByPrevCanvasIdAndIsDeleted(canvasId, IsDeleted.N)
                .orElse(null);

        // 삭제하는 canvas을 참조하고 있던 canvas의 prev 값을 현 삭제 canvas의 prev 값으로 수정
        if (prevLinkedCanvas != null) {
            prevLinkedCanvas.changePrevCanvas(canvas.getPrevCanvas());
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


    //    ========== websocket 소스코드 영역
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private Map<String, String> topics;

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

    public void enterChatRoom(String roomId) {
        String topic = topics.get(roomId);
        if (topic == null) {
            topics.put(roomId, roomId);
        }
    }

    public String getTopic(String roomId) {
        return topics.get(roomId);
    }

//    @KafkaListener(topics = "#{canvasService.getTopic(#roomId)}", groupId = "chat-group")
//    public void listenToMessages(String message) {
//        // 메시지 처리 로직 추가
//        System.out.println("Received Message: " + message);
//    }

    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    @KafkaListener(topics = "canvas-topic", groupId = "websocket-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumerProductQuantity(String message) { // return 시, string 형식으로 message가 들어옴
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(message);
            // ChatMessage 객채로 맵핑
            ChatMessage roomMessage = objectMapper.readValue(message, ChatMessage.class);
            messagingTemplate.convertAndSend("/sub/canvas/room/" + roomMessage.getRoomId(), roomMessage);
            SendCanvasReqDto sendCanvasReqDto = objectMapper.readValue(roomMessage.getMessage(), SendCanvasReqDto.class);
            editCanvasInSocket(sendCanvasReqDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
//            만약, 실패했을 때 코드 추가해야함
        }
        System.out.println(message);
    }

    public void editCanvasInSocket(SendCanvasReqDto sendCanvasReqDto) {
//        생성, 수정, 삭제인지 type 구분해서 넣어주는 용도
        if (sendCanvasReqDto.getMethod().equals(Method.create)) { // 생성 캔버스
            CreateCanvasReqDto createCanvasReqDto = sendCanvasReqDto.buildCreateCanvasReqDto();
            createCanvas(createCanvasReqDto, "");
        } else if (sendCanvasReqDto.getMethod().equals(Method.update)) { // 수정 캔버스
            UpdateCanvasReqDto updateCanvasReqDto = sendCanvasReqDto.buildUpdateCanvasReqDto();
            updateCanvas(updateCanvasReqDto);
        } else if (sendCanvasReqDto.getMethod().equals(Method.changeOrder)) { //순서 변경  캔버스
            ChangeOrderCanvasReqDto changeOrderCanvasReqDto = sendCanvasReqDto.buildChangeOrderCanvasReqDto();
            changeOrderCanvas(changeOrderCanvasReqDto);
        } else if (sendCanvasReqDto.getMethod().equals(Method.delete)) { // 삭제 캔버스
            deleteCanvas(sendCanvasReqDto.getCanvasId(), "");
        } else {
            log.error("잘못된 canvas method");
        }
    }

}
