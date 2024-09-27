package com.example.coconote.api.canvas.canvas.repository;

import com.example.coconote.api.canvas.canvas.entity.Canvas;
import com.example.coconote.api.channel.entity.Channel;
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
    Page<Canvas> findByChannelAndParentCanvasIdAndIsDeleted(Pageable pageable, Channel channel, Long parentCanvasId, IsDeleted isDeleted);
    List<Canvas> findByParentCanvasIdAndIsDeleted(Long parentCanvasId, IsDeleted isDeleted);
    List<Canvas> findByParentCanvasIdAndChannelAndIsDeleted(Long parentCanvasId, Channel channel, IsDeleted isDeleted);

}
