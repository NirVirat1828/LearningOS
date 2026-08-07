/**
 * Lays out one calendar month as weeks of 7 cells (Sun-Sat), padding the
 * leading/trailing edges with null so every week row is a full 7 wide.
 * Returns ISO date strings ("YYYY-MM-DD") to match what the backend's
 * DaySummary.date serializes as, so callers can look days up directly.
 */
export function buildMonthGrid(year: number, month: number): (string | null)[][] {
  const firstOfMonth = new Date(year, month - 1, 1);
  const daysInMonth = new Date(year, month, 0).getDate();
  const startWeekday = firstOfMonth.getDay();

  const cells: (string | null)[] = [];
  for (let i = 0; i < startWeekday; i++) {
    cells.push(null);
  }
  for (let day = 1; day <= daysInMonth; day++) {
    cells.push(`${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`);
  }
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }

  const weeks: (string | null)[][] = [];
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7));
  }
  return weeks;
}

export const MONTH_NAMES = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

export function formatDateLong(iso: string): string {
  const [year, month, day] = iso.split("-").map(Number);
  return `${MONTH_NAMES[month - 1]} ${day}, ${year}`;
}
