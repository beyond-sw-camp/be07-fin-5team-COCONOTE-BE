package com.example.coconote.api.canvas.block.service;

import com.example.coconote.api.canvas.block.dto.response.BlockListResDto;
import com.example.coconote.api.canvas.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.api.canvas.block.repository.BlockRepository;
import com.example.coconote.api.canvas.canvas.dto.request.CanvasSocketReqDto;
import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.canvas.canvas.service.CanvasService;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.CanvasBlockDocument;
import com.example.coconote.api.search.mapper.CanvasBlockMapper;
import com.example.coconote.api.canvas.canvas.entity.CanvasMessageMethod;
import com.example.coconote.api.canvas.canvas.repository.CanvasRepository;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockService {

    private final CanvasRepository canvasRepository; // 순환참조로 인해 service -> repository로 변경
    private final BlockRepository blockRepository;
    private final SearchService searchService;
    private final CanvasBlockMapper canvasBlockMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;


    @Transactional
    public CreateBlockResDto createBlock(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
        Canvas canvas = canvasRepository.findById(canvasSocketReqDto.getCanvasId()).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));

        Block checkBlock = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getBlockFeId(), IsDeleted.N).orElse(null);
        if (checkBlock != null) {
            throw new IllegalArgumentException("이미 있는 block 입니다.");
        }

        Block parentBlock = null;
        if (canvasSocketReqDto.getParentBlockId() != null) {
            // parentBlockId가 null이 아니면 findById 호출
            parentBlock = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getParentBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 부모 Block이 존재하지 않습니다."));
        }

        Block existingBlock = null; // 현재 생성하는 block의 prev block이 null일 때 첫번째 block 찾는 용

        Block prevBlock = null;
        if (canvasSocketReqDto.getPrevBlockId() != null) {
            prevBlock = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getPrevBlockId(), IsDeleted.N)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이전 Block이 존재하지 않습니다."));
        }else{
            // 생성하려는 block의 previd가 null 이면, 기존에 previd가 null인 block을 호출하여 현재 save 하려는 block의 id로 넣어줘야함
            existingBlock = blockRepository.findByCanvasIdAndPrevBlockIsNullAndIsDeleted(canvasSocketReqDto.getCanvasId(), IsDeleted.N) // prevBlockId가 null인 Block을 조회
                    .orElse(null); // prevBlock이 null인 블록을 찾음

        }


        // 나머지 Block 생성 로직
        Block block = Block.builder()
                .canvas(canvas)
                .contents(canvasSocketReqDto.getBlockContents())
                .feId(canvasSocketReqDto.getBlockFeId())
                .type(canvasSocketReqDto.getBlockType())
                .prevBlock(prevBlock)
                .parentBlock(parentBlock)
                .level(canvasSocketReqDto.getBlockLevel() != null && canvasSocketReqDto.getBlockLevel() > 0 ? canvasSocketReqDto.getBlockLevel() : 0)
                .indent(canvasSocketReqDto.getBlockIndent())
                .workspaceMember(workspaceMember)
                .build();

//        prev block 존재 및 이전에 해당 prev block을 갖고있는 block 주소 업데이트
        if (prevBlock != null) {
            Block originalPrevBlockHolder = blockRepository.findByPrevBlockIdAndIsDeleted(prevBlock.getId(), IsDeleted.N)
                    .orElse(null);
            if (originalPrevBlockHolder != null) {
                originalPrevBlockHolder.changePrevBlock(block);
            }
        }

        if (existingBlock != null) {
            existingBlock.changePrevBlock(block);
        }

        // Block 저장 및 리턴
        blockRepository.save(block);
//        검색 인덱스에 저장
//        CanvasBlockDocument document = canvasBlockMapper.toDocument(block);
//        IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(canvas.getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//        kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage);

        return CreateBlockResDto.fromEntity(block);
    }

    @Transactional
    public void updateBlock(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
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

//            CanvasBlockDocument document = canvasBlockMapper.toDocument(block);
//            IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//            kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @Transactional
    public void patchBlockDetails(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
        Block block = blockRepository.findByFeIdAndIsDeleted(canvasSocketReqDto.getBlockFeId(), IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 Block이 존재하지 않습니다."));
        if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.UPDATE_INDENT_BLOCK)) {
            block.patchBlockIndent(canvasSocketReqDto.getBlockIndent());
            blockRepository.save(block);
        }else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.HOT_UPDATE_CONTENTS_BLOCK)) {
            block.patchBlockContents(canvasSocketReqDto.getBlockContents());
            blockRepository.save(block);
        }
    }

    @Transactional
    public Boolean changeOrderBlock(CanvasSocketReqDto changeOrderBlockReqDto, WorkspaceMember workspaceMember) {
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

//            CanvasBlockDocument document = canvasBlockMapper.toDocument(originalNextBlock);
//            IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(originalNextBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//            kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());
        }

        // 4. 새로운 prevBlock과의 연결 설정
        if (newPrevBlock != null) {
            Block nextOfNewPrevBlock = blockRepository.findByPrevBlockFeIdAndIsDeleted(newPrevBlock.getFeId(), IsDeleted.N)
                    .orElse(null);

            // 새 prevBlock이 가지고 있던 nextBlock의 prevBlock을 currentBlock으로 설정
            if (nextOfNewPrevBlock != null && !nextOfNewPrevBlock.equals(currentBlock)) {
                nextOfNewPrevBlock.changePrevBlock(currentBlock);
                blockRepository.save(nextOfNewPrevBlock);

//                CanvasBlockDocument document = canvasBlockMapper.toDocument(nextOfNewPrevBlock);
//                IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(nextOfNewPrevBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//                kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());
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

//            CanvasBlockDocument document = canvasBlockMapper.toDocument(newNextBlock);
//            IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(newNextBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//            kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());
        }

        // 6. 현재 블록을 저장하여 순서 변경 적용
        blockRepository.save(currentBlock);

//        CanvasBlockDocument document = canvasBlockMapper.toDocument(currentBlock);
//        IndexEntityMessage<CanvasBlockDocument> indexEntityMessage = new IndexEntityMessage<>(currentBlock.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), EntityType.CANVAS_BLOCK, document);
//        kafkaTemplate.send("canvas_block_entity_search", indexEntityMessage.toJson());
        log.info("블록 순서가 성공적으로 변경되었습니다.");

        return true;
    }


    @Transactional
    public void deleteBlock(String feId) {
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
//        searchService.deleteBlock(block.getCanvas().getChannel().getSection().getWorkspace().getWorkspaceId(), block.getId());

    }

    @Transactional
    public void deepDeleteBlock(String feId) {
        Block block = blockRepository.findByFeIdAndIsDeleted(feId, IsDeleted.N)
                .orElseThrow(() -> new IllegalArgumentException("블록이 존재하지 않습니다."));
        Block prevLinkedBlock = blockRepository.findByPrevBlockFeIdAndIsDeleted(feId, IsDeleted.N)
                .orElse(null);

        // 삭제하는 block을 참조하고 있던 block의 prev 값을 현 삭제 block의 prev 값으로 수정
        if (prevLinkedBlock != null) {
            prevLinkedBlock.changePrevBlock(block.getPrevBlock());
        }

        List<Block> parentLinkedChildrenBlocks = blockRepository.findByParentBlockFeIdAndIsDeleted(feId, IsDeleted.N);

        // 자식 블록들도 재귀적으로 삭제
        deleteBlockAndChildren(block, parentLinkedChildrenBlocks);
    }

    @Transactional
    private void deleteBlockAndChildren(Block block, List<Block> children) {
        // 자식 블록이 있으면 재귀적으로 삭제
        if (children != null) {
            for (Block child : children) {
                List<Block> childBlocks = blockRepository.findByParentBlockFeIdAndIsDeleted(child.getFeId(), IsDeleted.N);
                deleteBlockAndChildren(child, childBlocks);
            }
        }

        // 블록을 물리적으로 삭제
        blockRepository.delete(block);
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
                .level(block.getLevel())
                .indent(block.getIndent())
                .workspaceMemberId(block.getWorkspaceMember() != null ? block.getWorkspaceMember().getWorkspaceMemberId() : 0)
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

    public void editBlockInSocket(CanvasSocketReqDto canvasSocketReqDto, WorkspaceMember workspaceMember) {
//        생성, 수정, 삭제인지 type 구분해서 넣어주는 용도
        if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CREATE_BLOCK)) { // 생성블록
            createBlock(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.UPDATE_BLOCK)) { // 수정블록
            updateBlock(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.UPDATE_INDENT_BLOCK)
                || canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.HOT_UPDATE_CONTENTS_BLOCK)) { // 수정블록
            patchBlockDetails(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.CHANGE_ORDER_BLOCK)) { //순서 변경 블록
            changeOrderBlock(canvasSocketReqDto, workspaceMember);
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.DELETE_BLOCK)) { // 삭제블록
            deleteBlock(canvasSocketReqDto.getBlockFeId());
        } else if (canvasSocketReqDto.getMethod().equals(CanvasMessageMethod.DEEP_DELETE_BLOCK)) { // 삭제블록
            deepDeleteBlock(canvasSocketReqDto.getBlockFeId());
        } else {
            log.error("잘못된 block method");
        }
    }


}
