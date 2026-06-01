/** Extract backend JSON `{ message: string }` (or nested Spring error shapes) from HttpClient errors. */
export function apiErrorMessage(err: unknown, fallback: string): string {
  if (!err || typeof err !== 'object') {
    return fallback;
  }
  const e = err as { error?: unknown; message?: unknown };
  if (typeof e.message === 'string' && e.message.trim()) {
    return e.message.trim();
  }
  const body = e.error;
  if (body && typeof body === 'object' && body !== null) {
    const o = body as Record<string, unknown>;
    const msg = o['message'];
    if (typeof msg === 'string' && msg.trim()) {
      return msg.trim();
    }
    const nested = o['error'];
    if (typeof nested === 'string' && nested.trim()) {
      return nested.trim();
    }
  }
  if (typeof body === 'string' && body.trim()) {
    return body.trim();
  }
  return fallback;
}
