import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ChatbotService, type ChatMessageDto } from '../../services/chatbot.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, ProgressSpinnerModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css'
})
export class ChatbotComponent implements OnInit {
  private readonly chatbot = inject(ChatbotService);

  readonly loading = signal(false);
  readonly question = signal('');
  readonly messages = signal<ChatMessageDto[]>([]);
  readonly suggestions = signal<string[]>([]);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadHistory();
    this.chatbot.suggestions().subscribe({
      next: (items) => this.suggestions.set(items),
      error: () => this.suggestions.set([])
    });
  }

  send(): void {
    const q = this.question().trim();
    if (!q || this.loading()) return;

    this.error.set(null);
    this.loading.set(true);
    this.question.set('');

    this.chatbot.ask(q).subscribe({
      next: () => this.loadHistory(),
      error: (err) => {
        this.error.set(apiErrorMessage(err, 'Chatbot request failed'));
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  useSuggestion(text: string): void {
    this.question.set(text);
    this.send();
  }

  private loadHistory(): void {
    this.chatbot.history().subscribe({
      next: (items) => this.messages.set(items),
      error: () => this.messages.set([])
    });
  }
}
