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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Block originalPrevBlockHolder = blockRepository.findByPrevBlockId(prevBlock.getId())
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
        if(prevLinkedBlock != null){
            prevLinkedBlock.changePrevBlock(block.getPrevBlock());
        }

        List<Block> parentLinkedChildrenBlocks = blockRepository.findByParentBlockFeIdAndIsDeleted(feId, IsDeleted.N);
        block.markAsDeleted(parentLinkedChildrenBlocks); // 실제 삭제 대신 소프트 삭제 처리
        searchService.deleteBlock(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), block.getId());

        return true;
    }

    public List<BlockListResDto> getBlockListFromCanvas(Long canvasId) {
        List<Block> blocks = blockRepository.findByCanvasIdAndIsDeleted(canvasId, IsDeleted.N);

        // 부모 블록을 기준으로 트리를 만들기 위한 Map 생성
        Map<String, BlockListResDto> blockMap = blocks.stream()
                .collect(Collectors.toMap(Block::getFeId, Block::fromEntity));

        // 부모-자식 관계 설정
        List<BlockListResDto> rootBlocks = new ArrayList<>();
        for (Block block : blocks) {
            BlockListResDto currentBlockDto = blockMap.get(block.getFeId());

            // rootBlocks 에서 block이 이미 존재하는지 확인
//            for (BlockListResDto rootBlock : rootBlocks) {
//                if (rootBlock.getFeId().equals(block.getFeId())) {
//                    // 이미 존재하면 해당 블록의 content만 업데이트
//                    rootBlock.setContent(currentBlockDto.getContent());
//                    continue; // 현재 루프를 끝내고 다음 블록으로
//                }
//            }

            if (block.getParentBlock() == null) {
                // 부모 블록이 없는 경우 루트 블록으로 간주
                rootBlocks.add(currentBlockDto);
            } else {
                // 부모 블록이 있는 경우 해당 부모의 자식 블록 리스트에 추가
                BlockListResDto parentBlockDto = blockMap.get(block.getParentBlock().getFeId());
                if (parentBlockDto != null) { // 부모블록이 있을 때
                    String prevBlockId = block.getPrevBlock() != null ? block.getPrevBlock().getFeId() : null;
                    insertBlockInOrder(parentBlockDto.getChildBlock(), currentBlockDto,
                            prevBlockId != null ? blockMap.get(prevBlockId) : null);
                } else { // 부모블록이 있지만 map에선 존재하지 않을 때
                    BlockListResDto tempParentBlockDto = BlockListResDto.builder()
                            .feId(block.getParentBlock().getFeId())
                            .build();

                    rootBlocks.add(tempParentBlockDto);
                }
            }
        }

        return rootBlocks;
    }

    // 형제 블록 사이의 올바른 위치에 삽입하는 메서드
    private void insertBlockInOrder(List<BlockListResDto> childBlocks, BlockListResDto newBlock, BlockListResDto prevBlock) {
        if (prevBlock == null) {
            // 이전 블록이 없으면 맨 앞에 삽입
            childBlocks.add(0, newBlock);
        } else {
            // 이전 블록이 있으면 그 뒤에 삽입
            int prevIndex = childBlocks.indexOf(prevBlock);
            childBlocks.add(prevIndex + 1, newBlock);
        }
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
