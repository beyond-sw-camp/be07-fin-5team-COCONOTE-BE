package com.example.coconote.api.drive.dto.response;

import com.example.coconote.api.drive.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderListDto {
    private Long folderId;
    private String folderName;

    public static List<FolderListDto> fromEntity(List<Folder> folderList) {
        return folderList.stream()
                .map(folder -> FolderListDto.builder()
                        .folderId(folder.getId())
                        .folderName(folder.getFolderName())
                        .build())
                .toList();
    }
}
