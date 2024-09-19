package com.example.coconote.api.canvas.entity;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.drive.entity.Folder;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Canvas extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_canvas_id")
    private Canvas parentCanvas;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

}
