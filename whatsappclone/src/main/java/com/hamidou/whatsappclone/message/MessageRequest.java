package com.hamidou.whatsappclone.message;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {

    public String content;
    public String senderId;
    public String receiverId;
    public MessageType type;
    public String chatId;

}
