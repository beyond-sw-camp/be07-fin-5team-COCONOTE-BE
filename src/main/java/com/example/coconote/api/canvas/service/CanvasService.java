package com.example.coconote.api.canvas.service;

import com.example.coconote.api.canvas.dto.request.CreateCanvasReqDto;
import com.example.coconote.api.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.dto.response.CreateCanvasResDto;
import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.api.canvas.repository.CanvasRepository;
import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.channel.repository.ChannelRepository;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CanvasService {
    private final CanvasRepository canvasRepository;
    private final ChannelRepository channelRepository;

    public CanvasService(CanvasRepository canvasRepository, ChannelRepository channelRepository){

        this.canvasRepository = canvasRepository;
        this.channelRepository = channelRepository;
    }

    @Transactional
    public CreateCanvasResDto createCanvas(CreateCanvasReqDto createCanvasReqDto, String email){
        Channel channel = channelRepository.findById(createCanvasReqDto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Canvas parentCanvas = null;
        // 부모 캔버스 조회 (parentCanvasId가 null이 아닐 경우에만)
        if (createCanvasReqDto.getParentCanvasId() != null) {
            parentCanvas = canvasRepository.findById(createCanvasReqDto.getParentCanvasId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 캔버스가 존재하지 않습니다."));

            // 부모 캔버스가 현재 채널에 속해 있는지 확인
            if (!parentCanvas.getChannel().getChannelId().equals(channel.getChannelId())) {
                throw new IllegalArgumentException("부모 캔버스가 현재 채널에 속해 있지 않습니다.");
            }
        }

        Canvas canvas = Canvas.builder()
                .title(createCanvasReqDto.getTitle())
                .parentCanvas(parentCanvas)
                .channel(channel)
                .build();

        canvasRepository.save(canvas);
        return CreateCanvasResDto.fromEntity(canvas);
    }

    public Page<CanvasListResDto> getCanvasListInChannel(Long channelId, String email, Pageable pageable, Integer depth){
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        Page<Canvas> canvasList = canvasRepository.findByChannelAndParentCanvasIdAndIsDeleted(pageable, channel, null, IsDeleted.N);


//        List<CanvasListResDto> childCanvas = null;
//        if(depth > 0){
//            for (int i = 0; i<depth; i++){
//                Canvas paerntCanvas = null;
//            }
//        }

        Page<CanvasListResDto> canvasListResDtos = canvasList.map(a -> a.fromListEntity());
        return canvasListResDtos;
    }

//    현 캔버스를 참조하고 있는 하위 캔버스
    public List<CanvasListResDto> getChildCanvasListFromCanvas(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        List<Canvas> childCanvas = canvasRepository.findByParentCanvasIdAndIsDeleted(canvas.getId(), IsDeleted.N);
        List<CanvasListResDto> childCanvasListDto = !childCanvas.isEmpty() ?
                childCanvas.stream().map(a->a.fromListEntity()).toList()
                : null;

        return childCanvasListDto;
    }

//    현 캔버스와 형제 캔버스
    public List<CanvasListResDto> getChildCanvasListFromParentCanvas(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        Canvas parentCanvas = null;
        if(canvas.getParentCanvas() != null){
            parentCanvas = canvasRepository.findByIdAndIsDeleted(canvas.getParentCanvas().getId(), IsDeleted.N).orElse(null);
        }

        List<Canvas> siblingCanvasList = canvasRepository.findByParentCanvasIdAndChannelAndIsDeleted(parentCanvas!=null ? parentCanvas.getId() : null,
                canvas.getChannel(), IsDeleted.N);
        List<CanvasListResDto> siblingCanvasListDto = !siblingCanvasList.isEmpty() ?
                siblingCanvasList.stream().map(a->a.fromListEntity()).toList()
                : null;


        return siblingCanvasListDto;
    }

    @Transactional
    public CanvasDetResDto getCanvasDetail(Long canvasId, String email){
        Canvas canvas = canvasRepository.findByIdAndIsDeleted(canvasId, IsDeleted.N).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        CanvasDetResDto canvasDetResDto = canvas.fromDetEntity();
        return canvasDetResDto;
    }

    @Transactional
    public void deleteCanvas(Long canvasId, String email) {
        Canvas canvas = canvasRepository.findById(canvasId)
                .orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
        canvas.markAsDeleted(); // 실제 삭제 대신 소프트 삭제 처리
    }


//    ========== 기능 불러와서 쓰기~

    public Canvas findByIdAndIsDeletedReturnRequired(Long canvasId){
        return canvasRepository.findById(canvasId).orElseThrow(() -> new IllegalArgumentException("캔버스가 존재하지 않습니다."));
    }

    public Canvas findByIdAndIsDeletedReturnOrElseNull(Long canvasId){
        return canvasRepository.findById(canvasId).orElse(null);
    }

}
