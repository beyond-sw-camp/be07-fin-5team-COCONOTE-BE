package com.example.coconote.global.fileUpload.dto.response;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.drive.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderLocationResDto {
    private Long folderId;
    private String folderName;
    private Long channelId;
    private String channelName;

    public static FolderLocationResDto fromEntity(Folder folder, Channel channel) {
        return FolderLocationResDto.builder()
                .folderId(folder.getId())
                .folderName(folder.getFolderName())
                .channelId(channel.getChannelId())
                .channelName(channel.getChannelName())
                .build();
    }
}
