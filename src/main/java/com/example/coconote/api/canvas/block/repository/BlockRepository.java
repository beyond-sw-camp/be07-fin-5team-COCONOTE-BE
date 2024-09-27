package com.example.coconote.api.canvas.block.repository;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block,Long> {
    Optional<Block> findByPrevBlockId(Long prevBlockId);
    List<Block> findByCanvasIdAndIsDeleted(Long canvasId, IsDeleted isDeleted);

    Optional<Block> findByFeId(String feId);

    Optional<Block> findByPrevBlockFeId(String feId);

    List<Block> findByParentBlockFeId(String feId);
}
