import type { Topic } from "../types/syllabus";
import type { DayDetail, DaySummary, MonthlyCalendarSummary } from "../types/calendar";
import { loadRoadmaps } from "./syllabusData";
import { getTopicProgress } from "./localProgress";

interface CompletedTopic {
  topic: Topic;
  date: string;
  minutes: number;
}

/**
 * Every topic marked COMPLETED, with the day it finished and the minutes it
 * counted for. Time falls back to the topic's estimatedMinutes when no actual
 * time was logged — so a completed topic always contributes something to the
 * calendar's intensity, even before time tracking is fleshed out.
 */
async function completedTopics(): Promise<CompletedTopic[]> {
  const roadmaps = await loadRoadmaps();
  const out: CompletedTopic[] = [];
  for (const roadmap of roadmaps) {
    for (const course of roadmap.courses) {
      for (const module of course.modules) {
        for (const topic of module.topics) {
          if (topic.completionStatus === "COMPLETED" && topic.completedDate) {
            const stored = getTopicProgress(topic.id);
            out.push({
              topic,
              date: topic.completedDate,
              minutes: stored.timeSpentMinutes ?? topic.estimatedMinutes,
            });
          }
        }
      }
    }
  }
  return out;
}

export async function computeMonthlySummary(year: number, month: number): Promise<MonthlyCalendarSummary> {
  const done = await completedTopics();
  const prefix = `${year}-${String(month).padStart(2, "0")}-`;

  const byDate = new Map<string, { topics: number; minutes: number }>();
  for (const entry of done) {
    if (!entry.date.startsWith(prefix)) continue;
    const acc = byDate.get(entry.date) ?? { topics: 0, minutes: 0 };
    acc.topics += 1;
    acc.minutes += entry.minutes;
    byDate.set(entry.date, acc);
  }

  const days: DaySummary[] = [...byDate.entries()].map(([date, v]) => ({
    date,
    topicsFinished: v.topics,
    minutesSpent: v.minutes,
  }));

  return { year, month, days };
}

export async function computeDayDetail(date: string): Promise<DayDetail> {
  const done = await completedTopics();
  const onDay = done.filter((entry) => entry.date === date);
  return {
    date,
    topicsFinished: onDay.map((entry) => entry.topic),
    totalMinutesSpent: onDay.reduce((sum, entry) => sum + entry.minutes, 0),
  };
}
