package com.example.coconote.api.thread.threadTag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadTagReqDto {
    private Long threadId;
    private Long tagId;
}
