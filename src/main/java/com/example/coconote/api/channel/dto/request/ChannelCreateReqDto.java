package com.example.coconote.api.channel.dto.request;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.section.entity.Section;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelCreateReqDto {
    private Long sectionId;
    private String name;
    private String info;
    private boolean isPublic;

    public Channel toEntity(Section section) {
        return Channel.builder()
                .section(section)
                .name(this.name)
                .info(this.info)
                .isPublic(this.isPublic)
                .build();
    }
}
