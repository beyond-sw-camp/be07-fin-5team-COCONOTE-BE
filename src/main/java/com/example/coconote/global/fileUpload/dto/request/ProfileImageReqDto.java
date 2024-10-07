package com.example.coconote.global.fileUpload.dto.request;

import com.example.coconote.global.fileUpload.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileImageReqDto {
    private Long workspaceMemberId;
    private String profileImage;
}
