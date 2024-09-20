package com.example.coconote.api.tag.dto.request;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.api.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateReqDto {
    private Long tagId;
    private String updateTagName;
}
