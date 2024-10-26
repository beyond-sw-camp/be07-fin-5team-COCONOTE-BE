package com.example.coconote.api.thread.tag.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TagSearchReqListDto {
    private Long channelId;
    private List<TagSearchId> tagSearchIds;



    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class TagSearchId {
        private Long tagId;
    }
}
