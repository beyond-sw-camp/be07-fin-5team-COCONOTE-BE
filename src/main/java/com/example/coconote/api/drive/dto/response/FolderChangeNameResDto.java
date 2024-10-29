package com.example.coconote.api.drive.dto.response;

import com.example.coconote.api.drive.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FolderChangeNameResDto {
    private Long folderId;
    private String folderName;
    private Long channelId;
    private Long parentFolderId;

    public static FolderChangeNameResDto fromEntity(Folder folder) {
        return FolderChangeNameResDto.builder()
                .folderId(folder.getId())
                .folderName(folder.getFolderName())
                .channelId(folder.getChannel().getChannelId())
                .parentFolderId(folder.getParentFolder() == null ? null : folder.getParentFolder().getId())
                .build();
    }
}
