package com.example.coconote.api.block.controller;

import com.example.coconote.api.block.dto.request.CreateBlockReqDto;
import com.example.coconote.api.block.dto.response.CreateBlockResDto;
import com.example.coconote.api.block.service.BlockService;
import com.example.coconote.api.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @Operation(
            summary = "Block 생성",
            description = "새로운 Block 생성."
    )
    @PostMapping("/create")
    public ResponseEntity<?> createBlock(@RequestBody CreateBlockReqDto createBlockReqDto, String email){
        CreateBlockResDto createBlockResDto = blockService.createBlock(createBlockReqDto, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "Block이 성공적으로 생성되었습니다.", createBlockResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }
}
