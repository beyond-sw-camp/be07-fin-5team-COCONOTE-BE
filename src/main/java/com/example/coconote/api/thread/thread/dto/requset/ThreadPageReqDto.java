package com.example.coconote.api.thread.thread.dto.requset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadPageReqDto {
    private Long channelId;
    private Long threadId;
    private Long pageSize;
}
