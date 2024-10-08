package com.example.coconote.api.thread.tag.dto.request;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.thread.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagCreateReqDto {
    private String name;
    private String color;
    private Long channelId;

    public Tag toEntity(Channel channel) {
        return Tag.builder()
                .name(this.name)
                .color(this.color)
                .channel(channel)
                .build();
    }
}
