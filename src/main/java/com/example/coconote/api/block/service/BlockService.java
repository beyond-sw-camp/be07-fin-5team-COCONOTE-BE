package com.example.coconote.api.block.service;

import com.example.coconote.api.block.dto.request.CreateBlockReqDto;
import com.example.coconote.api.block.dto.request.UpdateBlockReqDto;
import com.example.coconote.api.block.dto.response.BlockListResDto;
import com.example.coconote.api.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.block.entity.Block;
import com.example.coconote.api.block.repository.BlockRepository;
import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.canvas.service.CanvasService;
import com.example.coconote.common.IsDeleted;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BlockService {

    private final CanvasService canvasService;
    private final BlockRepository blockRepository;

    public BlockService(CanvasService canvasService, BlockRepository blockRepository){
        this.canvasService = canvasService;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public CreateBlockResDto createBlock(CreateBlockReqDto createBlockReqDto, String email){
        Canvas canvas = canvasService.findByIdAndIsDeletedReturnRequired(createBlockReqDto.getCanvasId());

        Block parentBlock = null;
        if (createBlockReqDto.getParentBlockId() != null) {
            // parentBlockId가 null이 아니면 findById 호출
            parentBlock = blockRepository.findById(createBlockReqDto.getParentBlockId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 부모 Block이 존재하지 않습니다."));
        }

        Block prevBlock = null;
        if(createBlockReqDto.getPrevBlockId() != null){
            prevBlock = blockRepository.findById(createBlockReqDto.getPrevBlockId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 이전 Block이 존재하지 않습니다."));


        }


        // 나머지 Block 생성 로직
        Block block = Block.builder()
                .canvas(canvas)
                .contents(createBlockReqDto.getContents())
                .prevBlock(prevBlock)
                .parentBlock(parentBlock)
                .build();

//        prev block 존재 및 이전에 해당 prev block을 갖고있는 block 주소 업데이트
        if(prevBlock != null){
            Block originalPrevBlockHolder = blockRepository.findByPrevBlockId(prevBlock.getId())
                    .orElse(null);
            if(originalPrevBlockHolder != null){
                originalPrevBlockHolder.changePrevBlock(block);
            }
        }

        // Block 저장 및 리턴
        blockRepository.save(block);

        return CreateBlockResDto.fromEntity(block);
    }

    @Transactional
    public Boolean updateBlock(UpdateBlockReqDto updateBlockReqDto, String email){
        Block block = blockRepository.findById(updateBlockReqDto.getBlockId()).orElseThrow(() -> new IllegalArgumentException("해당 Block이 존재하지 않습니다."));
        Block prevBlock = updateBlockReqDto.getPrevBlockId() != null
                ? blockRepository.findById(updateBlockReqDto.getPrevBlockId())
                .orElseThrow(() -> new IllegalArgumentException("해당 Prev Block이 존재하지 않습니다."))
                : null;

        //        prev block 존재 및 이전에 해당 prev block을 갖고있는 block 주소 업데이트
        if(prevBlock != null){
            Block originalPrevBlockHolder = blockRepository.findByPrevBlockId(prevBlock.getId())
                    .orElse(null);
            if(originalPrevBlockHolder != null){
                originalPrevBlockHolder.changePrevBlock(block);
            }
        }

        Block parentBlock = updateBlockReqDto.getParentBlockId() != null
                ? blockRepository.findById(updateBlockReqDto.getParentBlockId())
                .orElseThrow(() -> new IllegalArgumentException("해당 Parent Block이 존재하지 않습니다."))
                : null;

        block.updateAllInfo(prevBlock, parentBlock, updateBlockReqDto.getContents());

        return true;
    }

    public List<BlockListResDto> getBlockListFromCanvas(Long canvasId, String email){
        List<Block> blocks = blockRepository.findByCanvasIdAndIsDeleted(canvasId, IsDeleted.N);

        // 부모 블록을 기준으로 트리를 만들기 위한 Map 생성
        Map<Long, BlockListResDto> blockMap = blocks.stream()
                .collect(Collectors.toMap(Block::getId, Block::fromEntity));

        // 부모-자식 관계 설정
        List<BlockListResDto> rootBlocks = new ArrayList<>();
        for (Block block : blocks) {
            BlockListResDto currentBlockDto = blockMap.get(block.getId());
            if (block.getParentBlock() == null) {
                // 부모 블록이 없는 경우 루트 블록으로 간주
                rootBlocks.add(currentBlockDto);
            } else {
                // 부모 블록이 있는 경우 해당 부모의 자식 블록 리스트에 추가
                BlockListResDto parentBlockDto = blockMap.get(block.getParentBlock().getId());
                if (parentBlockDto != null) {
                    Long prevBlockId = block.getPrevBlock() != null ? block.getPrevBlock().getId() : null;
                    insertBlockInOrder(parentBlockDto.getChildBlock(), currentBlockDto,
                            prevBlockId != null ? blockMap.get(prevBlockId) : null);
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

}
