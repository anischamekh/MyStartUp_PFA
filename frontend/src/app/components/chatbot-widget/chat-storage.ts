import type { ChatMessageDto } from '../../services/chatbot.service';

const STORAGE_KEY = 'mystartup-chat-widget-messages';
const UNREAD_KEY = 'mystartup-chat-widget-unread';

export function loadStoredMessages(): ChatMessageDto[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as ChatMessageDto[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveStoredMessages(messages: ChatMessageDto[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
  } catch {
    /* ignore quota errors */
  }
}

export function loadUnreadCount(): number {
  try {
    const raw = localStorage.getItem(UNREAD_KEY);
    return raw ? Math.max(0, parseInt(raw, 10) || 0) : 0;
  } catch {
    return 0;
  }
}

export function saveUnreadCount(count: number): void {
  try {
    localStorage.setItem(UNREAD_KEY, String(Math.max(0, count)));
  } catch {
    /* ignore */
  }
}
