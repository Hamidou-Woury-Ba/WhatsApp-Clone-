import {Component, OnDestroy, OnInit} from '@angular/core';
import { ChatListComponent } from '../../components/chat-list/chat-list.component';
import { ChatResponse, MessageRequest, MessageResponse } from '../../services/models';
import { ChatService, MessageService } from '../../services/services';
import { KeycloakService } from '../../utils/keycloak/keycloak.service';
import { DatePipe } from '@angular/common';
import { PickerComponent } from '@ctrl/ngx-emoji-mart';
import { EmojiData } from '@ctrl/ngx-emoji-mart/ngx-emoji';
import { FormsModule } from '@angular/forms';
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';
import { Notification } from './models/notification';
import * as console from 'node:console';

@Component({
  selector: 'app-main',
  imports: [ChatListComponent, DatePipe, FormsModule, PickerComponent],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit, OnDestroy {

  chats: Array<ChatResponse> = [];
  selectedChat: ChatResponse = {};
  chatMessages: MessageResponse[] = [];
  showEmojis = false;
  messageContent = '';
  socketClient: any = null;
  private notificationSubscription: any;

  constructor(
    private chatService: ChatService,
    private keycloakService: KeycloakService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.initWebSocket();
    this.getAllChats();
  }

  private getAllChats() {
    this.chatService.getChatsByReceiver()
      .subscribe({
        next: (res) => {
          this.chats = res;
        }
      })
  }

  onNewChatCreated(newChat: ChatResponse) {
    // Vérifier si le chat n'existe pas déjà
    const existingChat = this.chats.find(chat => chat.id === newChat.id);
    if (!existingChat) {
      // Ajouter le nouveau chat en début de liste
      this.chats.unshift(newChat);
    }
    // Sélectionner automatiquement le nouveau chat
    this.chatSelected(newChat);
  }

  logout() {
    this.keycloakService.logout();
  }

  userProfile() {
    this.keycloakService.accountManagement();
  }

  chatSelected(chatResponse: ChatResponse) {
    this.selectedChat = chatResponse;
    this.getAllChatMessages(chatResponse.id as string);
    this.setMessagesToSeen();
    this.selectedChat.unreadCount = 0;
  }

  private getAllChatMessages(chatId: string) {
    this.messageService.getMessages({
      'chat-id': chatId
    }).subscribe({
      next: (messages) => {
        this.chatMessages = messages;
      }
    })
  }

  private setMessagesToSeen() {
    this.messageService.setMessageToSeen({
      'chat-id': this.selectedChat.id as string
    }).subscribe({
      next: () => { }
    })
  }

  isSelfMessage(message: MessageResponse) {
    return message.senderId === this.keycloakService.userId;
  }

  uploadMedia(target: EventTarget | null) {

  }

  onSelectEmojis(emojiSelected: any) {
    const emoji: EmojiData = emojiSelected.emoji;
    this.messageContent += emoji.native;
  }

  keyDown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  }

  onClick() {
    this.setMessagesToSeen();
  }

  sendMessage() {
    if (this.messageContent) {
      const messageRequest: MessageRequest = {
        chatId: this.selectedChat.id,
        senderId: this.getSenderId(),
        receiverId: this.getReceiverId(),
        content: this.messageContent,
        type: 'TEXT'
      }
      this.messageService.saveMessage({
        body: messageRequest
      }).subscribe({
        next: () => {
          const messageResponse: MessageResponse = {
            senderId: this.getSenderId(),
            receiverId: this.getReceiverId(),
            content: this.messageContent,
            type: 'TEXT',
            state: 'SENT',
            createdAt: new Date().toString()
          };
          this.selectedChat.lastMessage = this.messageContent;
          this.chatMessages.push(messageResponse);
          this.moveToTop(this.selectedChat);
          this.messageContent = '';
          this.showEmojis = false;
        }
      })
    }
  }

  private moveToTop(chat: ChatResponse) {
    const index = this.chats.findIndex(c => c.id === chat.id);
    if (index > 0) {
      this.chats.splice(index, 1);
      this.chats.unshift(chat);
    }
  }

  private getSenderId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.senderId as string;
    }
    return this.selectedChat.receiverId as string;
  }

  private getReceiverId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.receiverId as string;
    }
    return this.selectedChat.senderId as string;
  }

  private initWebSocket() {
    if (this.keycloakService.keycloak.tokenParsed?.sub) {
      let ws = new SockJS('http://localhost:8080/ws');
      this.socketClient = Stomp.over(ws);
      const subUrl = `/user/${this.keycloakService.keycloak.tokenParsed?.sub}/chat`;
      this.socketClient.connect({
        'Authorization': `Bearer ${this.keycloakService.keycloak.token}`
      }, () => {
        this.notificationSubscription = this.socketClient.subscribe(subUrl, (message: any) => {
          const notification: Notification = JSON.parse(message.body);
          this.handleNotification(notification);
        },
          () => {
            console.log("Error while connecting to websocket");
          }
        );
      });
    }
  }

  private handleNotification(notification: Notification) {
    if(!notification) return;
    if(this.selectedChat && this.selectedChat.id === notification.chatId){
      switch (notification.type) {
        case 'MESSAGE':
        case 'IMAGE':
          const message: MessageResponse = {
            senderId: notification.senderId,
            receiverId: notification.receiverId,
            content: notification.content,
            type: notification.messageType,
            media: notification.media,
            createdAt: new Date().toString()
          };
          if(notification.type == "IMAGE"){
            this.selectedChat.lastMessage = "Attachment";
          }else{
            this.selectedChat.lastMessage = notification.content;
          }
          this.chatMessages.push(message);
          this.moveToTop(this.selectedChat);
          break;
        case 'SEEN':
          this.chatMessages.forEach(m => m.state = 'SEEN')
          break;
      }
    }else{
      const destChat = this.chats.find(c => c.id === notification.chatId);
      if(destChat && notification.type !== "SEEN"){
        if(notification.type === "MESSAGE"){
          destChat.lastMessage = notification.content;
        }else if (notification.type === "IMAGE"){
          destChat.lastMessage = "Attachment";
        }
        destChat.lastMessageTime = new Date().toString();
        destChat.unreadCount! += 1;
      }else if(notification.type === "MESSAGE"){
        const newChat: ChatResponse = {
          id: notification.chatId,
          senderId: notification.senderId,
          receiverId: notification.receiverId,
          lastMessage: notification.content,
          name: notification.chatName,
          unreadCount: 1,
          lastMessageTime: new Date().toString(),
        }
        this.chats.unshift(newChat);
      }
    }
  }

  ngOnDestroy(): void {
    if(this.socketClient !== null){
      this.socketClient.disconnect();
      this.notificationSubscription.unsubscribe();
      this.socketClient = null;
    }
  }

}
