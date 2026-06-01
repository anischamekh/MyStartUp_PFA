import {
  AfterViewChecked,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { ChatbotService, type ChatMessageDto } from '../../services/chatbot.service';
import { apiErrorMessage } from '../../utils/api-error';
import { loadStoredMessages, loadUnreadCount, saveStoredMessages, saveUnreadCount } from './chat-storage';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [FormsModule, ButtonModule],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.css'
})
export class ChatbotWidgetComponent implements OnInit, AfterViewChecked {
  private readonly chatbot = inject(ChatbotService);

  @ViewChild('messagesEl') private messagesEl?: ElementRef<HTMLDivElement>;

  readonly open = signal(false);
  readonly loading = signal(false);
  readonly question = signal('');
  readonly messages = signal<ChatMessageDto[]>([]);
  readonly suggestions = signal<string[]>([]);
  readonly error = signal<string | null>(null);
  readonly unreadCount = signal(0);

  private scrollPending = false;
  private messageCountBeforeReply = 0;

  ngOnInit(): void {
    this.messages.set(loadStoredMessages());
    this.unreadCount.set(loadUnreadCount());
    this.loadHistory();
    this.chatbot.suggestions().subscribe({
      next: (items) => this.suggestions.set(items),
      error: () => this.suggestions.set([])
    });
  }

  ngAfterViewChecked(): void {
    if (this.scrollPending) {
      this.scrollToBottom();
      this.scrollPending = false;
    }
  }

  toggleOpen(): void {
    const next = !this.open();
    this.open.set(next);
    if (next) {
      this.unreadCount.set(0);
      saveUnreadCount(0);
      this.scrollPending = true;
      this.loadHistory();
    }
  }

  close(): void {
    this.open.set(false);
  }

  send(): void {
    const q = this.question().trim();
    if (!q || this.loading()) return;

    this.error.set(null);
    this.loading.set(true);
    this.question.set('');
    this.messageCountBeforeReply = this.messages().length;

    const optimistic: ChatMessageDto = {
      id: -Date.now(),
      userId: 0,
      role: 'USER',
      content: q,
      fromUser: true,
      createdAt: new Date().toISOString()
    };
    this.appendMessages([...this.messages(), optimistic]);

    this.chatbot.ask(q).subscribe({
      next: () => this.loadHistory(true),
      error: (err) => {
        this.error.set(apiErrorMessage(err, 'Chatbot request failed'));
        this.loading.set(false);
      }
    });
  }

  useSuggestion(text: string): void {
    this.question.set(text);
    this.send();
  }

  onInputKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  formatTime(iso: string): string {
    try {
      return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  }

  private loadHistory(trackUnread = false): void {
    this.chatbot.history().subscribe({
      next: (items) => {
        this.appendMessages(items);
        if (trackUnread && !this.open()) {
          const newAiMessages = items
            .slice(this.messageCountBeforeReply)
            .filter((m) => !m.fromUser).length;
          if (newAiMessages > 0) {
            const nextUnread = this.unreadCount() + newAiMessages;
            this.unreadCount.set(nextUnread);
            saveUnreadCount(nextUnread);
          }
        }
        this.scrollPending = true;
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private appendMessages(items: ChatMessageDto[]): void {
    const sorted = [...items].sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
    this.messages.set(sorted);
    saveStoredMessages(sorted);
  }

  private scrollToBottom(): void {
    const el = this.messagesEl?.nativeElement;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }
}
