const SETTINGS_KEY = "learningos.settings.v1";
const PLAN_PREFIX = "learningos.plan.";

const DEFAULT_DAILY_MINUTES = 90;

interface Settings {
  dailyMinutes: number;
}

function readSettings(): Settings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) {
      return JSON.parse(raw) as Settings;
    }
  } catch {
    /* fall through to default */
  }
  return { dailyMinutes: DEFAULT_DAILY_MINUTES };
}

export function getDailyMinutes(): number {
  const value = readSettings().dailyMinutes;
  return Number.isFinite(value) && value > 0 ? value : DEFAULT_DAILY_MINUTES;
}

export function setDailyMinutes(minutes: number): void {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify({ dailyMinutes: minutes }));
}

/**
 * Today's plan is persisted (topic ids for one date) so it stays a stable,
 * finishable list through the day even as topics get checked off — rather
 * than reshuffling on every refresh. Carry-forward is implicit: an unfinished
 * topic is still actionable tomorrow, so the next day's fresh computation
 * simply picks it again.
 */
export function getStoredPlan(date: string): string[] | null {
  try {
    const raw = localStorage.getItem(PLAN_PREFIX + date);
    return raw ? (JSON.parse(raw) as string[]) : null;
  } catch {
    return null;
  }
}

export function setStoredPlan(date: string, topicIds: string[]): void {
  localStorage.setItem(PLAN_PREFIX + date, JSON.stringify(topicIds));
}

/** Drop the stored plan for a date so it gets recomputed — used when the budget changes. */
export function clearStoredPlan(date: string): void {
  localStorage.removeItem(PLAN_PREFIX + date);
}

export function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}
