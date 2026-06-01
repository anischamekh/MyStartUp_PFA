/** Parse YYYY-MM-DD (or ISO prefix) to local Date at midnight. */
export function parseYmd(s: string | null | undefined): Date | null {
  if (!s) return null;
  const head = s.split('T')[0];
  if (!/^\d{4}-\d{2}-\d{2}$/.test(head)) return null;
  const [y, m, d] = head.split('-').map(Number);
  return new Date(y, m - 1, d);
}

/** Format local Date to YYYY-MM-DD for API payloads. */
export function toYmd(d: Date | null | undefined): string {
  if (!d) return '';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
