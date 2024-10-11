package com.example.coconote.api.canvas.canvas.controller;


import com.example.coconote.api.canvas.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.api.canvas.canvas.dto.request.UpdateCanvasReqDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.canvas.canvas.service.CanvasService;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> createCanvas(@RequestBody CreateCanvasReqDto createCanvasReqDto
            , @AuthenticationPrincipal CustomPrincipal customPrincipal){
        CreateCanvasResDto createCanvasResDto = canvasService.createCanvas(createCanvasReqDto, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "Canvas가 성공적으로 생성되었습니다.", createCanvasResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "채널 내 Canvas 리스트 - 1depth",
            description = "채널 내 Canvas 리스트 확인하기 - 1depth 짜리만 조회"
    )
    @GetMapping("/{channelId}/list")
    public ResponseEntity<?> getCanvasListInChannel(@PathVariable Long channelId, String email,
                                                    @PageableDefault(page = 0, size = 50, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                                                    @RequestParam(defaultValue = "0") Integer depth){
        Page<CanvasListResDto> canvasListResDto = canvasService.getCanvasListInChannel(channelId, email, pageable, depth);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas가 성공적으로 조회되었습니다.", canvasListResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(
            summary = "현 캔버스를 참조하고 있는 하위 캔버스",
            description = "현 캔버스를 parentCanvas 로 참조 하고 있는 하위 Canvas 리스트 확인하기"
    )
    @GetMapping("/{canvasId}/list/child")
    public ResponseEntity<?> getChildCanvasListFromCanvas(@PathVariable Long canvasId, String email){
//        하위 캔버스는 전체 노출시키는 형식으로 진행
        List<CanvasListResDto> canvasListResDto = canvasService.getChildCanvasListFromCanvas(canvasId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas 기준으로 리스트가 성공적으로 조회되었습니다.", canvasListResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(
            summary = "현 캔버스와 형제 캔버스",
            description = "현 캔버스의 parentCanvas 를 참조 하고 있는 하위 Canvas 리스트 확인하기"
    )
    @GetMapping("/{canvasId}/list/sibling")
    public ResponseEntity<?> getChildCanvasListFromParentCanvas(@PathVariable Long canvasId, String email){
        List<CanvasListResDto> canvasListResDto = canvasService.getChildCanvasListFromParentCanvas(canvasId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas의 parentCanvas 기준으로 리스트가 성공적으로 조회되었습니다.", canvasListResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Canvas 상세 읽기",
            description = "Canvas 상세 읽기."
    )
    @GetMapping("/{canvasId}")
    public ResponseEntity<?> getCanvasDetail(@PathVariable Long canvasId, String email){
        CanvasDetResDto canvasDetResDto = canvasService.getCanvasDetail(canvasId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas가 성공적으로 조회되었습니다.", canvasDetResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Canvas 수정하기",
            description = "Canvas 수정하기."
    )
    @PatchMapping("/{canvasId}")
    public ResponseEntity<?> updateCanvas(@RequestBody UpdateCanvasReqDto updateCanvasReqDto){
        CanvasDetResDto canvasDetResDto = canvasService.updateCanvas(updateCanvasReqDto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas가 성공적으로 업데이트되었습니다.", canvasDetResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Canvas 삭제하기",
            description = "Canvas 삭제하기."
    )
    @DeleteMapping("/{canvasId}")
    public ResponseEntity<?> deleteCanvas(@PathVariable Long canvasId, String email){
        canvasService.deleteCanvas(canvasId, email);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Canvas가 성공적으로 삭제되었습니다.", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
