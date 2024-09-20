package com.example.coconote.api.block.repository;

import com.example.coconote.api.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block,Long> {

}
