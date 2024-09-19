package com.example.coconote.api.canvas.repository;

import com.example.coconote.api.canvas.entity.Canvas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanvasRepository extends JpaRepository<Canvas,Long> {

}
