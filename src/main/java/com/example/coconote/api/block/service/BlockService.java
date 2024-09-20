package com.example.coconote.api.block.service;

import com.example.coconote.api.block.dto.request.CreateBlockReqDto;
import com.example.coconote.api.block.dto.request.UpdateBlockReqDto;
import com.example.coconote.api.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.block.entity.Block;
import com.example.coconote.api.block.repository.BlockRepository;
import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.canvas.service.CanvasService;
import jakarta.persistence.EntityNotFoundException;
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


        // 나머지 Block 생성 로직
        Block block = Block.builder()
                .canvas(canvas)
                .contents(createBlockReqDto.getContents())
                .nextBlock(null)
                .parentBlock(parentBlock)
                .build();

        // Block 저장 및 리턴
        blockRepository.save(block);

        return CreateBlockResDto.fromEntity(block);
    }



}
