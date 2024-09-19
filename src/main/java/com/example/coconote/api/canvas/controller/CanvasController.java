package com.example.coconote.api.canvas.controller;


import com.example.coconote.api.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.api.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.canvas.service.CanvasService;
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
@RequestMapping("/api/v1/canvas")
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasService canvasService;

    @Operation(
            summary = "Canvas 생성",
            description = "새로운 Canvas 생성. `parentCanvasId`가 null이면 최상위 Canvas로 간주"
    )
    @PostMapping("/create")
    public ResponseEntity<?> createCanvas(@RequestBody CreateCanvasReqDto createCanvasReqDto, String email){
        CreateCanvasResDto createCanvasResDto = canvasService.createCanvas(createCanvasReqDto, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "Canvas가 성공적으로 생성되었습니다.", createCanvasResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }
}
