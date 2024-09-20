package com.example.coconote.api.threadTag.entity;

import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.tag.entity.Tag;
import com.example.coconote.api.thread.entity.Thread;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Thread thread;
    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;

    public ThreadTag(Thread thread, Tag tag) {
        this.thread = thread;
        this.tag = tag;
    }

    public TagResDto fromEntity() {
        return TagResDto.builder()
                .id(this.tag.getId())
                .name(this.tag.getName())
                .color(this.tag.getColor())
                .build();
    }
}
