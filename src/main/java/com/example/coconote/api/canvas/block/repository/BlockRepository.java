package com.example.coconote.api.canvas.block.repository;

import com.example.coconote.api.canvas.block.entity.Block;
import com.example.coconote.common.IsDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByPrevBlockIdAndIsDeleted(Long prevBlockId, IsDeleted isDeleted);

    List<Block> findByCanvasIdAndIsDeleted(Long canvasId, IsDeleted isDeleted);

    Optional<Block> findByFeIdAndIsDeleted(String feId, IsDeleted isDeleted);

    Optional<Block> findByPrevBlockFeIdAndIsDeleted(String feId, IsDeleted isDeleted);

    List<Block> findByParentBlockFeIdAndIsDeleted(String feId, IsDeleted isDeleted);

    // parentBlock의 feId를 이용해 자식 블록을 찾는 쿼리
    List<Block> findByParentBlockFeId(String parentBlockFeId);

    Optional<Block> findByCanvasIdAndIsDeletedAndPrevBlock_FeIdAndParentBlock_FeId(Long canvasId, IsDeleted isDeleted, String prevBlockFeId, String parentBlockFeId);

    Optional<Block> findByCanvasIdAndIsDeletedAndPrevBlockFeId(Long canvasId, String prevBlockFeId, IsDeleted isDeleted);
}
