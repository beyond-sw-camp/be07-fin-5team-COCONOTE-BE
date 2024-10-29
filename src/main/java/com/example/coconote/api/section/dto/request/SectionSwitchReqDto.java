package com.example.coconote.api.section.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionSwitchReqDto {
    private Long sectionId;
    private Long fromIndex;
    private Long toIndex;
}
