package com.example.coconote.api.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
    private Long workspaceId;
    private Long channelId;
    private NotificationDto notification;

}
