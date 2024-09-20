package com.example.coconote.api.canvas.repository;

import com.example.coconote.api.canvas.entity.Canvas;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanvasRepository extends JpaRepository<Canvas,Long> {
    Optional<Canvas> findByIdAndIsDeleted(Long canvasId, IsDeleted isDeleted);
    Page<Canvas> findByChannelIdAndParentCanvasId(Pageable pageable, Long channelId, Long parentCanvasId);
    Page<Canvas> findByChannelIdAndParentCanvasIdAndIsDeleted(Pageable pageable, Long channelId, Long parentCanvasId, IsDeleted isDeleted);
    List<Canvas> findByParentCanvasId(Long parentCanvasId);
    List<Canvas> findByParentCanvasIdAndIsDeleted(Long parentCanvasId, IsDeleted isDeleted);
    List<Canvas> findByParentCanvasIdAndChannelId(Long parentCanvasId, Long channelId);
    List<Canvas> findByParentCanvasIdAndChannelIdAndIsDeleted(Long parentCanvasId, Long channelId, IsDeleted isDeleted);

}
