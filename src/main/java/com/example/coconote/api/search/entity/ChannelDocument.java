package com.example.coconote.api.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDocument {
    @Id
    private String channelId;  // OpenSearch 문서 ID
    private String channelName;
    private String channelInfo;
    private Boolean isPublic;
    private Long sectionId;
}

