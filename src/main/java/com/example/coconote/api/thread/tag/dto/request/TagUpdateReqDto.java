package com.example.coconote.api.thread.tag.dto.request;

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
