package com.example.coconote.api.canvas.repository;

import com.example.coconote.api.canvas.entity.Canvas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasRepository extends JpaRepository<Canvas,Long> {
    Page<Canvas> findByChannelIdAndParentCanvasId(Pageable pageable, Long channelId, Long parentCanvasId);
    List<Canvas> findByParentCanvasId(Long parentCanvasId);
}
