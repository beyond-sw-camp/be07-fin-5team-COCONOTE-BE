package com.example.coconote.api.thread.tag.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagResDto {
    private Long id;
    private String name;
    private String color;
    private Long threadTagId;
}
