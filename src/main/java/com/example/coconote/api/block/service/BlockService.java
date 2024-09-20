package com.example.coconote.api.block.service;

import com.example.coconote.api.block.dto.request.CreateBlockReqDto;
import com.example.coconote.api.block.dto.request.UpdateBlockReqDto;
import com.example.coconote.api.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.block.entity.Block;
import com.example.coconote.api.block.repository.BlockRepository;
import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.canvas.service.CanvasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Block parentBlock = updateBlockReqDto.getParentBlockId() != null
                ? blockRepository.findById(updateBlockReqDto.getParentBlockId())
                .orElseThrow(() -> new IllegalArgumentException("해당 Parent Block이 존재하지 않습니다."))
                : null;

        block.updateAllInfo(prevBlock, parentBlock, updateBlockReqDto.getContents());

        return true;
    }

}
