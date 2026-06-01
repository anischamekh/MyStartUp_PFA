import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';

export interface ChatMessageDto {
  id: number;
  userId: number;
  role: string;
  content: string;
  fromUser: boolean;
  createdAt: string;
}

export interface ChatResponseDto {
  answer: string;
  suggestedQuestions: string[];
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private readonly api = inject(ApiService);

  ask(question: string) {
    return this.api.client.post<ChatResponseDto>(`${this.api.baseUrl}/chatbot/ask`, { question });
  }

  history() {
    return this.api.client.get<ChatMessageDto[]>(`${this.api.baseUrl}/chatbot/history`);
  }

  suggestions() {
    return this.api.client.get<string[]>(`${this.api.baseUrl}/chatbot/suggestions`);
  }
}
