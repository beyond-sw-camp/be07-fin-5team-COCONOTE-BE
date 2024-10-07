package com.example.coconote.api.canvas.block.service;

import com.example.coconote.api.canvas.block.dto.request.CreateBlockReqDto;
import com.example.coconote.api.canvas.block.dto.request.SendBlockReqDto;
import com.example.coconote.api.canvas.block.dto.request.UpdateBlockReqDto;
import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.block.entity.Method;
import com.example.coconote.api.canvas.block.entity.Type;
import com.example.coconote.api.canvas.block.repository.BlockRepository;
import com.example.coconote.api.canvas.canvas.dto.request.ChatMessage;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.service.CanvasService;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.common.IsDeleted;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.search.Search;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BlockService {

    private final CanvasService canvasService;
    private final BlockRepository blockRepository;
    private final SearchService searchService;

    public BlockService(CanvasService canvasService, BlockRepository blockRepository, SearchService searchService, SimpMessageSendingOperations messagingTemplate, KafkaTemplate<String, Object> kafkaTemplate) {
        this.canvasService = canvasService;
        this.blockRepository = blockRepository;
        this.searchService = searchService;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public CreateBlockResDto createBlock(CreateBlockReqDto createBlockReqDto, String email) {
        Canvas canvas = canvasService.findByIdAndIsDeletedReturnRequired(createBlockReqDto.getCanvasId());

        Block parentBlock = null;
        if (createBlockReqDto.getParentBlockId() != null) {
            // parentBlockId가 null이 아니면 findById 호출
            parentBlock = blockRepository.findByFeIdAndIsDeleted(createBlockReqDto.getParentBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 부모 Block이 존재하지 않습니다."));
        }

        Block prevBlock = null;
        if (createBlockReqDto.getPrevBlockId() != null) {
            prevBlock = blockRepository.findByFeIdAndIsDeleted(createBlockReqDto.getPrevBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이전 Block이 존재하지 않습니다."));


        }


        // 나머지 Block 생성 로직
        Block block = Block.builder()
                .canvas(canvas)
                .contents(createBlockReqDto.getContents())
                .feId(createBlockReqDto.getFeId())
                .type(createBlockReqDto.getType())
                .prevBlock(prevBlock)
                .parentBlock(parentBlock)
                .build();

//        prev block 존재 및 이전에 해당 prev block을 갖고있는 block 주소 업데이트
        if (prevBlock != null) {
            Block originalPrevBlockHolder = blockRepository.findByPrevBlockIdAndIsDeleted(prevBlock.getId(), IsDeleted.N)
                    .orElse(null);
            if (originalPrevBlockHolder != null) {
                originalPrevBlockHolder.changePrevBlock(block);
            }
        }

        // Block 저장 및 리턴
        blockRepository.save(block);
//        검색 인덱스에 저장
        searchService.indexBlock(canvas.getChannel().getSection().getWorkspace().getWorkspaceId(), block);

        return CreateBlockResDto.fromEntity(block);
    }

    @Transactional
    public Boolean updateBlock(UpdateBlockReqDto updateBlockReqDto, String email) {
        try {
            Block block = blockRepository.findByFeIdAndIsDeleted(updateBlockReqDto.getFeId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Block이 존재하지 않습니다."));
            Block prevBlock = updateBlockReqDto.getPrevBlockId() != null
                    ? blockRepository.findByFeIdAndIsDeleted(updateBlockReqDto.getPrevBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Prev Block이 존재하지 않습니다."))
                    : null;

            //        prev block 존재 및 이전에 해당 prev block을 갖고있는 block 주소 업데이트
            if (prevBlock != null) {
                Block originalPrevBlockHolder = blockRepository.findByPrevBlockFeIdAndIsDeleted(prevBlock.getFeId(), IsDeleted.N)
                        .orElse(null);
                if (originalPrevBlockHolder != null) {
                    originalPrevBlockHolder.changePrevBlock(block);
                }
            }

            Block parentBlock = updateBlockReqDto.getParentBlockId() != null
                    ? blockRepository.findByFeIdAndIsDeleted(updateBlockReqDto.getParentBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Parent Block이 존재하지 않습니다."))
                    : null;

            block.updateAllInfo(prevBlock, parentBlock, updateBlockReqDto.getContents());
            blockRepository.save(block);
            searchService.indexBlock(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), block);

        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return true;
    }

    @Transactional
    public Boolean deleteBlock(String feId, String email) {
        Block block = blockRepository.findByFeIdAndIsDeleted(feId, IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("블록이 존재하지 않습니다."));
        Block prevLinkedBlock = blockRepository.findByPrevBlockFeIdAndIsDeleted(feId, IsDeleted.N)
                .orElse(null);

        // 삭제하는 block을 참조하고 있던 block의 prev 값을 현 삭제 block의 prev 값으로 수정
        if (prevLinkedBlock != null) {
            prevLinkedBlock.changePrevBlock(block.getPrevBlock());
        }

        List<Block> parentLinkedChildrenBlocks = blockRepository.findByParentBlockFeIdAndIsDeleted(feId, IsDeleted.N);
        block.markAsDeleted(parentLinkedChildrenBlocks); // 실제 삭제 대신 소프트 삭제 처리
        searchService.deleteBlock(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), block.getId());

        return true;
    }


    public List<BlockListResDto> getBlockListFromCanvas(Long canvasId) {
//        List<Block> blocks = blockRepository.findByCanvasIdAndIsDeleted(canvasId, IsDeleted.N);

        List<BlockListResDto> blockResult = new ArrayList<>();

        Block firstBlock = blockRepository.findByCanvasIdAndIsDeletedAndPrevBlock_FeIdAndParentBlock_FeId(
                canvasId,
                IsDeleted.N,
                null,
                null
        ).orElseThrow(() -> new EntityNotFoundException("첫번째 순서의 블록을 찾을 수 없습니다."));

        blockResult.add(firstBlock.fromEntity());

        Boolean isWhile = true;
        Block prevTargetBlock = firstBlock.copy();
        while (isWhile) {
            Block block = blockRepository.findByPrevBlockFeIdAndIsDeleted(prevTargetBlock.getFeId(), IsDeleted.N).orElse(null);
            if(block == null){ // 더이상 넣을 값이 없음 (마지막 순서의 block
                isWhile = false;
                break;
            }
            prevTargetBlock = block.copy();
            blockResult.add(block.fromEntity());
        }

        return blockResult;
    }

    //    ================= 통신전용
    private final SimpMessageSendingOperations messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private Map<String, String> topics;

    @PostConstruct
    private void init() {
        topics = new HashMap<>();
    }

    public void enterChatRoom(String roomId) {
        String topic = topics.get(roomId);
        if (topic == null) {
            topics.put(roomId, roomId);
        }
    }

    @Transactional
    @KafkaListener(topics = "block-topic", groupId = "websocket-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumerProductQuantity(String message) { // return 시, string 형식으로 message가 들어옴
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // ChatMessage 객채로 맵핑
            ChatMessage roomMessage = objectMapper.readValue(message, ChatMessage.class);
            messagingTemplate.convertAndSend("/sub/block/room/" + roomMessage.getRoomId(), roomMessage);
            SendBlockReqDto sendBlockReqDto = objectMapper.readValue(roomMessage.getMessage(), SendBlockReqDto.class);
            editBlockInSocket(sendBlockReqDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            //            만약, 실패했을 때 코드 추가해야함
        }
        System.out.println(message);
    }

    public void editBlockInSocket(SendBlockReqDto sendBlockReqDto) {
//        생성, 수정, 삭제인지 type 구분해서 넣어주는 용도
        if (sendBlockReqDto.getMethod().equals(Method.create)) { // 생성블록
            CreateBlockReqDto createBlockReqDto = sendBlockReqDto.buildCreateBlockReqDto();
            createBlock(createBlockReqDto, "");
        } else if (sendBlockReqDto.getMethod().equals(Method.update)) { // 수정블록
            UpdateBlockReqDto updateBlockReqDto = sendBlockReqDto.buildUpdateBlockReqDto();
            updateBlock(updateBlockReqDto, "");
        } else if (sendBlockReqDto.getMethod().equals(Method.delete)) { // 삭제블록
            deleteBlock(sendBlockReqDto.getFeId(), "");
//            log.info("삭제블록 제작 진행 중");
        } else {
            log.error("잘못된 block method");
        }
    }


}
