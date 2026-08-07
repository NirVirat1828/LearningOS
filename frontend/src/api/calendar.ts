import type { DayDetail, MonthlyCalendarSummary } from "../types/calendar";
import { computeDayDetail, computeMonthlySummary } from "../lib/calendarData";

// Backendless build: the calendar is computed from localStorage progress +
// static syllabus metadata (see lib/calendarData.ts), not fetched from a REST
// API. Kept as thin wrappers so the hooks importing them didn't change shape.

export async function fetchMonthlySummary(year: number, month: number): Promise<MonthlyCalendarSummary> {
  return computeMonthlySummary(year, month);
}

export async function fetchDayDetail(date: string): Promise<DayDetail> {
  return computeDayDetail(date);
}
