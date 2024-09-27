package com.example.coconote.api.section.dto.response;

import com.example.coconote.api.channel.channel.dto.response.ChannelResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionListResDto {
    private Long sectionId;
    private String sectionName;
    private List<String> channelNameList;
}
