import { Component, OnInit } from '@angular/core';
import { ChatListComponent } from '../../components/chat-list/chat-list.component';
import { ChatResponse, MessageRequest, MessageResponse } from '../../services/models';
import { ChatService, MessageService } from '../../services/services';
import { KeycloakService } from '../../utils/keycloak/keycloak.service';
import { DatePipe } from '@angular/common';
import { PickerComponent } from '@ctrl/ngx-emoji-mart';
import { EmojiData } from '@ctrl/ngx-emoji-mart/ngx-emoji';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-main',
  imports: [ChatListComponent, DatePipe, FormsModule, PickerComponent],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit {

  chats: Array<ChatResponse> = [];
  selectedChat: ChatResponse = {};
  chatMessages: MessageResponse[] = [];
  showEmojis = false;
  messageContent = '';

  constructor(
    private chatService: ChatService,
    private keycloakService: KeycloakService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
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

}
