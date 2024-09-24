package com.example.coconote.api.tag.entity;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tag_id")
    private Long id;
    @Column(name="tag_name")
    private String name;
    @Column(name="tag_color")
    private String color;
    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    public TagResDto fromEntity() {
        return TagResDto.builder()
                .id(this.id)
                .name(this.name)
                .color(this.color).build();
    }

    public Tag updateName(String updateTagName) {
        this.name = updateTagName;
        return this;
    }

    public void deleteTag() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }
}
