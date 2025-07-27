package com.hamidou.whatsappclone.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private String senderId;
    private String receiverId;
    private MessageType type;
    private MessageState state;
    private LocalDateTime createdAt;
    private byte[] media;

}
