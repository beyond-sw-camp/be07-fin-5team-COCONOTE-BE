package com.example.coconote.api.canvas.canvas.service;

import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.canvas.canvas.dto.request.ChatMessage;
import com.example.coconote.api.canvas.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.common.IsDeleted;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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

@Service
@Transactional(readOnly = true)
public class CanvasService {
    private final CanvasRepository canvasRepository;
    private final ChannelRepository channelRepository;

    public CanvasService(CanvasRepository canvasRepository, ChannelRepository channelRepository, KafkaTemplate<String, Object> kafkaTemplate, SimpMessageSendingOperations messagingTemplate){
        this.canvasRepository = canvasRepository;
        this.channelRepository = channelRepository;

//        websocket 용도
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public CreateCanvasResDto createCanvas(CreateCanvasReqDto createCanvasReqDto, String email){
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

        Canvas canvas = Canvas.builder()
                .title(createCanvasReqDto.getTitle())
                .parentCanvas(parentCanvas)
                .channel(channel)
                .build();

        canvasRepository.save(canvas);

        topics.put(String.valueOf(canvas.getId()), String.valueOf(canvas.getId()));

        return CreateCanvasResDto.fromEntity(canvas);
    }

    public Page<CanvasListResDto> getCanvasListInChannel(Long channelId, String email, Pageable pageable, Integer depth){
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
    public List<CanvasListResDto> getChildCanvasListFromCanvas(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        List<Canvas> childCanvas = canvasRepository.findByParentCanvasIdAndIsDeleted(canvas.getId(), IsDeleted.N);
        List<CanvasListResDto> childCanvasListDto = !childCanvas.isEmpty() ?
                childCanvas.stream().map(a->a.fromListEntity()).toList()
                : null;

        return childCanvasListDto;
    }

//    현 캔버스와 형제 캔버스
    public List<CanvasListResDto> getChildCanvasListFromParentCanvas(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        Canvas parentCanvas = null;
        if(canvas.getParentCanvas() != null){
            parentCanvas = canvasRepository.findByIdAndIsDeleted(canvas.getParentCanvas().getId(), IsDeleted.N).orElse(null);
        }

        List<Canvas> siblingCanvasList = canvasRepository.findByParentCanvasIdAndChannelAndIsDeleted(parentCanvas!=null ? parentCanvas.getId() : null,
                canvas.getChannel(), IsDeleted.N);
        List<CanvasListResDto> siblingCanvasListDto = !siblingCanvasList.isEmpty() ?
                siblingCanvasList.stream().map(a->a.fromListEntity()).toList()
                : null;


        return siblingCanvasListDto;
    }

    @Transactional
    public CanvasDetResDto getCanvasDetail(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        CanvasDetResDto canvasDetResDto = canvas.fromDetEntity();
        return canvasDetResDto;
    }

    @Transactional
    public void deleteCanvas(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findById(canvasId)
                .orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        canvas.markAsDeleted(); // 실제 삭제 대신 소프트 삭제 처리
    }


//    ========== 기능 불러와서 쓰기~

    public Canvas findByIdAndIsDeletedReturnRequired(Long canvasId){
        return canvasRepository.findById(canvasId).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
    }

    public Canvas findByIdAndIsDeletedReturnOrElseNull(Long canvasId){
        return canvasRepository.findById(canvasId).orElse(null);
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

    @KafkaListener(topics = "canvas-topic", groupId = "websocket-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumerProductQuantity(String message){ // return 시, string 형식으로 message가 들어옴
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(message);
            // ChatMessage 객채로 맵핑
            ChatMessage roomMessage =  objectMapper.readValue(message,ChatMessage.class);
            messagingTemplate.convertAndSend("/sub/canvas/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
//            만약, 실패했을 때 코드 추가해야함
        }
        System.out.println(message);
    }

}
