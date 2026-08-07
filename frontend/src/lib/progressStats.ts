import type { Topic } from "../types/syllabus";
import { loadRoadmaps } from "./syllabusData";
import { getTopicProgress } from "./localProgress";

export type TopicState = "completed" | "ready" | "locked";

export interface TopicEntry {
  topic: Topic;
  roadmapTitle: string;
  courseId: string;
  courseTitle: string;
  state: TopicState;
}

export interface CourseSummary {
  courseId: string;
  courseTitle: string;
  roadmapTitle: string;
  total: number;
  completed: number;
  ready: number;
  locked: number;
  topics: TopicEntry[];
}

export interface ProgressStats {
  totalTopics: number;
  completed: number;
  ready: number;
  locked: number;
  minutesInvested: number;
  activeDays: number;
  courses: CourseSummary[];
}

/**
 * Single pass over the whole topic pool + localStorage progress that both the
 * Dashboard and Backlog read from. A topic is "ready" when it isn't completed
 * and every prerequisite IS completed (the same gate the planner uses),
 * "locked" when a prerequisite is still outstanding.
 */
export async function computeProgressStats(): Promise<ProgressStats> {
  const roadmaps = await loadRoadmaps();

  const flat: { topic: Topic; roadmapTitle: string; courseId: string; courseTitle: string }[] = [];
  for (const roadmap of roadmaps) {
    for (const course of roadmap.courses) {
      for (const module of course.modules) {
        for (const topic of module.topics) {
          flat.push({ topic, roadmapTitle: roadmap.title, courseId: course.id, courseTitle: course.title });
        }
      }
    }
  }

  const completedIds = new Set(
    flat.filter((f) => f.topic.completionStatus === "COMPLETED").map((f) => f.topic.id),
  );

  const stateOf = (topic: Topic): TopicState => {
    if (completedIds.has(topic.id)) return "completed";
    return topic.prerequisites.every((p) => completedIds.has(p)) ? "ready" : "locked";
  };

  const courses = new Map<string, CourseSummary>();
  let minutesInvested = 0;
  const activeDates = new Set<string>();

  for (const f of flat) {
    const state = stateOf(f.topic);
    if (state === "completed") {
      const stored = getTopicProgress(f.topic.id);
      minutesInvested += stored.timeSpentMinutes ?? f.topic.estimatedMinutes;
      if (f.topic.completedDate) activeDates.add(f.topic.completedDate);
    }

    let course = courses.get(f.courseId);
    if (!course) {
      course = {
        courseId: f.courseId,
        courseTitle: f.courseTitle,
        roadmapTitle: f.roadmapTitle,
        total: 0,
        completed: 0,
        ready: 0,
        locked: 0,
        topics: [],
      };
      courses.set(f.courseId, course);
    }
    course.total += 1;
    course[state] += 1;
    course.topics.push({
      topic: f.topic,
      roadmapTitle: f.roadmapTitle,
      courseId: f.courseId,
      courseTitle: f.courseTitle,
      state,
    });
  }

  return {
    totalTopics: flat.length,
    completed: completedIds.size,
    ready: flat.filter((f) => stateOf(f.topic) === "ready").length,
    locked: flat.filter((f) => stateOf(f.topic) === "locked").length,
    minutesInvested,
    activeDays: activeDates.size,
    courses: [...courses.values()],
  };
}
