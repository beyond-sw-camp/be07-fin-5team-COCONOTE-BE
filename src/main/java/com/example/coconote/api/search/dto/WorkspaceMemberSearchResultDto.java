package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberSearchResultDto {
    private Long workspaceMemberId;
    private String memberName;
    private String email;
    private String profileImage;
}
