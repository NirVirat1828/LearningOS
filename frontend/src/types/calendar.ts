import type { Topic } from "./syllabus";

// Topic-centric in the backendless build: a topic IS the unit of work, so
// "activity on a day" means topics completed that day (there is no separate
// Task entity here the way the old backend had).

export interface DaySummary {
  date: string;
  topicsFinished: number;
  minutesSpent: number;
}

export interface MonthlyCalendarSummary {
  year: number;
  month: number;
  days: DaySummary[];
}

export interface DayDetail {
  date: string;
  topicsFinished: Topic[];
  totalMinutesSpent: number;
}
