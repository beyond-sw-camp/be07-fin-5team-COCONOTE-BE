package com.example.coconote.api.drive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveFolderReqDto {
    private Long folderId;
//    이동할 폴더의 id
    private Long parentId;
}
