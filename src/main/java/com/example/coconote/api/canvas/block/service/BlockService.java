package com.example.coconote.api.canvas.block.service;

import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.block.repository.BlockRepository;
import com.example.coconote.api.canvas.canvas.dto.request.CanvasSocketReqDto;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.entity.CanvasMessageMethod;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.common.IsDeleted;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BlockService {

    private final CanvasRepository canvasRepository; // 순환참조로 인해 service -> repository로 변경
    private final BlockRepository blockRepository;
    private final SearchService searchService;

    public BlockService(CanvasRepository canvasRepository, BlockRepository blockRepository, SearchService searchService, SimpMessageSendingOperations messagingTemplate, KafkaTemplate<String, Object> kafkaTemplate) {
        this.canvasRepository = canvasRepository;
        this.blockRepository = blockRepository;
        this.searchService = searchService;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public CreateBlockResDto createBlock(CanvasSocketReqDto canvasSocketReqDto) {
        Canvas canvas = canvasRepository.findById(canvasSocketReqDto.getCanvasId()).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));

        Block parentBlock = null;
        if (canvasSocketReqDto.getParentBlockId() != null) {
            // parentBlockId가 null이 아니면 findById 호출
            parentBlock = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getParentBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 부모 Block이 존재하지 않습니다."));
        }

        Block prevBlock = null;
        if (canvasSocketReqDto.getPrevBlockId() != null) {
            prevBlock = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getPrevBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이전 Block이 존재하지 않습니다."));


        }


        // 나머지 Block 생성 로직
        Block block = Block.builder()
                .canvas(canvas)
                .contents(canvasSocketReqDto.getBlockContents())
                .feId(canvasSocketReqDto.getBlockFeId())
                .type(canvasSocketReqDto.getBlockType())
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
    public Boolean updateBlock(CanvasSocketReqDto canvasSocketReqDto) {
        try {
            Block block = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getBlockFeId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Block이 존재하지 않습니다."));
            Block prevBlock = canvasSocketReqDto.getPrevBlockId() != null
                    ? blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getPrevBlockId(), IsDeleted.N)
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

            Block parentBlock = canvasSocketReqDto.getParentBlockId() != null
                    ? blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getParentBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Parent Block이 존재하지 않습니다."))
                    : null;

            block.updateAllInfo(prevBlock, parentBlock, canvasSocketReqDto.getBlockContents());
            blockRepository.save(block);
            searchService.indexBlock(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), block);

        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return true;
    }

    @Transactional
    public Boolean changeOrderBlock(CanvasSocketReqDto changeOrderBlockReqDto) {
        log.info("순서 변경!! ChangeOrderBlockReqDto {}", changeOrderBlockReqDto);

        // 1. feId로 현재 블록 찾기
        Block currentBlock = blockRepository.findByFeIdAndIsDeleted(changeOrderBlockReqDto.getBlockFeId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Block이 존재하지 않습니다."));

        // 2. 새로운 prevBlockId와 nextBlockId로 블록 찾기 (없으면 null 허용)
        Block newPrevBlock = null;
        if (changeOrderBlockReqDto.getPrevBlockId() != null) {
            newPrevBlock = blockRepository.findByFeIdAndIsDeleted(changeOrderBlockReqDto.getPrevBlockId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 앞 블록일 수 있음)
        }

        Block newNextBlock = null;
        if (changeOrderBlockReqDto.getNextBlockId() != null) {
            newNextBlock = blockRepository.findByFeIdAndIsDeleted(changeOrderBlockReqDto.getNextBlockId(), IsDeleted.N)
                    .orElse(null); // null 허용 (맨 뒤 블록일 수 있음)
        }

        // 3. 기존 prevBlock과 nextBlock 연결 끊기
            Block originalPrevBlock = currentBlock.getPrevBlock();
            Block originalNextBlock = blockRepository.findByPrevBlockFeIdAndIsDeleted(currentBlock.getFeId(), IsDeleted.N)
                    .orElse(null);

            // 기존 prevBlock이 연결한 nextBlock을 업데이트
            if (originalNextBlock != null) {
                originalNextBlock.changePrevBlock(originalPrevBlock);
                blockRepository.save(originalNextBlock);
                searchService.indexBlock(currentBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), originalNextBlock);

            }

        // 4. 새로운 prevBlock과의 연결 설정
        if (newPrevBlock != null) {
            Block nextOfNewPrevBlock = blockRepository.findByPrevBlockFeIdAndIsDeleted(newPrevBlock.getFeId(), IsDeleted.N)
                    .orElse(null);

            // 새 prevBlock이 가지고 있던 nextBlock의 prevBlock을 currentBlock으로 설정
            if (nextOfNewPrevBlock != null && !nextOfNewPrevBlock.equals(currentBlock)) {
                nextOfNewPrevBlock.changePrevBlock(currentBlock);
                blockRepository.save(nextOfNewPrevBlock);
                searchService.indexBlock(currentBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), nextOfNewPrevBlock);

            }

            // 현재 블록의 prevBlock을 새로운 prevBlock으로 설정
            currentBlock.changePrevBlock(newPrevBlock);
        } else {
            // 새 prevBlock이 없다면, 현재 블록을 첫 번째 블록으로 만듭니다.
            currentBlock.changePrevBlock(null);
        }

        // 5. 새로운 nextBlock과의 연결 설정
        if (newNextBlock != null) {
            newNextBlock.changePrevBlock(currentBlock);
            blockRepository.save(newNextBlock);
            searchService.indexBlock(currentBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), newNextBlock);

        }

        // 6. 현재 블록을 저장하여 순서 변경 적용
        blockRepository.save(currentBlock);
        searchService.indexBlock(currentBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), currentBlock);
        log.info("블록 순서가 성공적으로 변경되었습니다.");
        return true;
    }


    @Transactional
    public Boolean deleteBlock(String feId) {
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
        // 1. 데이터베이스에서 모든 블록을 가져옴
        List<Block> blocks = blockRepository.findByCanvasIdAndIsDeleted(canvasId, IsDeleted.N);

        // 2. 블록들을 Map에 저장 (id -> Block)
        Map<Long, Block> blockMap = blocks.stream()
                .collect(Collectors.toMap(Block::getId, block -> block));

        // 3. 블록을 저장할 최종 리스트
        List<BlockListResDto> result = new ArrayList<>();
        Set<Long> visitedBlocks = new HashSet<>(); // 중복 방지를 위한 Set

        // 4. 루트 블록 찾기: prev_block_fe_id가 null인 블록
        for (Block block : blocks) {
            if (block.getPrevBlock() == null) {
                // 루트 블록이면 리스트에 추가
                BlockListResDto rootBlockDto = convertToDto(block);
                result.add(rootBlockDto);
                visitedBlocks.add(block.getId());

                // 루트 블록을 기준으로 자식과 형제 블록 추가
                addChildBlocks(block, result, blockMap, visitedBlocks);
                addSiblingBlocks(block, result, blockMap, visitedBlocks);
            }
        }

        return result;
    }

    private BlockListResDto convertToDto(Block block) {
        return BlockListResDto.builder()
                .id(block.getId())
                .content(block.getContents())
                .feId(block.getFeId())
                .type(block.getType())
                .member(block.getMember())
                .prevBlockFeId(block.getPrevBlock() != null ? block.getPrevBlock().getFeId() : null)
                .build();
    }

    // 재귀적으로 자식 블록을 추가하는 메서드
    private void addChildBlocks(Block parentBlock, List<BlockListResDto> result, Map<Long, Block> blockMap, Set<Long> visitedBlocks) {
        for (Block block : blockMap.values()) {
            if (block.getParentBlock() != null && block.getParentBlock().getId().equals(parentBlock.getId())) {
                // 이미 방문한 블록이면 건너뜀
                if (visitedBlocks.contains(block.getId())) {
                    continue;
                }

                // 부모가 현재 블록인 자식 블록을 찾고 리스트에 추가
                BlockListResDto childBlockDto = convertToDto(block);
                result.add(childBlockDto);
                visitedBlocks.add(block.getId());

                // 자식 블록을 기준으로 다시 자식 블록을 추가 (재귀 호출)
                addChildBlocks(block, result, blockMap, visitedBlocks);

                // 자식 블록의 형제 블록을 추가 (재귀 호출)
                addSiblingBlocks(block, result, blockMap, visitedBlocks);
            }
        }
    }

    // 재귀적으로 형제 블록을 prev_block_fe_id에 따라 추가하는 메서드
    private void addSiblingBlocks(Block currentBlock, List<BlockListResDto> result, Map<Long, Block> blockMap, Set<Long> visitedBlocks) {
        for (Block block : blockMap.values()) {
            if (block.getPrevBlock() != null && block.getPrevBlock().getId().equals(currentBlock.getId())) {
                // 이미 방문한 블록이면 건너뜀
                if (visitedBlocks.contains(block.getId())) {
                    continue;
                }

                // prev_block_fe_id가 현재 블록인 형제 블록을 리스트에 추가
                BlockListResDto siblingBlockDto = convertToDto(block);
                result.add(siblingBlockDto);
                visitedBlocks.add(block.getId());

                // 형제 블록을 기준으로 다시 자식 블록 추가 (재귀 호출)
                addChildBlocks(block, result, blockMap, visitedBlocks);

                // 형제 블록의 형제 블록을 추가 (재귀 호출)
                addSiblingBlocks(block, result, blockMap, visitedBlocks);
            }
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

//    block topic은 이제 사용 X
//    @Transactional
//    @KafkaListener(topics = "block-topic", groupId = "websocket-group"
//            , containerFactory = "kafkaListenerContainerFactory")
//    public void consumerProductQuantity(String message) { // return 시, string 형식으로 message가 들어옴
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            // ChatMessage 객채로 맵핑
//            CanvasSocketReqDto roomMessage = objectMapper.readValue(message, ChatMessage.class);
//            messagingTemplate.convertAndSend("/sub/block/room/" + roomMessage.getRoomId(), roomMessage);
////            SendBlockReqDto sendBlockReqDto = objectMapper.readValue(roomMessage.getMessage(), SendBlockReqDto.class);
//            editBlockInSocket(roomMessage);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            //            만약, 실패했을 때 코드 추가해야함
//        }
//        System.out.println(message);
//    }

    public void editBlockInSocket(CanvasSocketReqDto canvasSocketReqDto) {
//        생성, 수정, 삭제인지 type 구분해서 넣어주는 용도
        if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CREATE_BLOCK)) { // 생성블록
            createBlock(canvasSocketReqDto);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.UPDATE_BLOCK)) { // 수정블록
            updateBlock(canvasSocketReqDto);
        } else if(canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CHANGE_ORDER_BLOCK)){ //순서 변경 블록
            changeOrderBlock(canvasSocketReqDto);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.DELETE_BLOCK)) { // 삭제블록
            deleteBlock(canvasSocketReqDto.getBlockFeId());
        } else {
            log.error("잘못된 block method");
        }
    }


}
