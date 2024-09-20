package com.example.coconote.api.threadTag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadTagAddReqDto {
    private Long threadId;
    private Long tagId;
}
