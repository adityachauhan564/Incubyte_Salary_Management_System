/**
 * Local-date <-> ISO-date-string conversion that deliberately avoids
 * `Date.toISOString()` / `new Date("2026-01-01")`, both of which parse or
 * format through UTC and can silently shift the date by one day depending
 * on the browser's timezone offset from UTC.
 */
export function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function parseIsoDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}
