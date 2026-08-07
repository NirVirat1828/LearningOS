import { useProgressStats } from "../hooks/useProgressStats";
import { useSetTopicComplete } from "../hooks/useSetTopicComplete";
import StatTile from "../components/StatTile";
import CollapsibleSection from "../components/syllabus/CollapsibleSection";
import PlannerTopicRow from "../components/planner/PlannerTopicRow";
import type { CourseSummary } from "../lib/progressStats";
import type { Topic } from "../types/syllabus";

export default function Backlogs() {
  const { data: stats, isLoading, isError } = useProgressStats();
  const setComplete = useSetTopicComplete();

  function toggle(topic: Topic) {
    setComplete.mutate({
      topicId: topic.id,
      completed: topic.completionStatus !== "COMPLETED",
      estimatedMinutes: topic.estimatedMinutes,
    });
  }

  // Only courses that still have unfinished work belong in a backlog.
  const openCourses = (stats?.courses ?? []).filter((c) => c.ready + c.locked > 0);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold text-slate-900">Backlog</h1>
      <p className="mt-2 text-slate-500">Everything still ahead — what's ready to start, and what's still locked.</p>

      {isLoading && <p className="mt-6 text-slate-500">Loading backlog...</p>}
      {isError && <p className="mt-6 text-rose-600">Couldn't load the backlog.</p>}

      {stats && (
        <>
          <div className="mt-6 grid grid-cols-3 gap-3 sm:max-w-md">
            <StatTile label="Ready" value={stats.ready} />
            <StatTile label="Locked" value={stats.locked} />
            <StatTile label="Completed" value={stats.completed} />
          </div>

          {openCourses.length === 0 && (
            <p className="mt-6 rounded-lg border border-dashed border-slate-200 p-6 text-center text-sm text-slate-400">
              Your backlog is empty — every topic is complete. 🎉
            </p>
          )}

          <div className="mt-6 flex flex-col gap-3">
            {openCourses.map((course) => (
              <CourseBacklog key={course.courseId} course={course} onToggle={toggle} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function CourseBacklog({ course, onToggle }: { course: CourseSummary; onToggle: (topic: Topic) => void }) {
  const ready = course.topics.filter((t) => t.state === "ready");
  const locked = course.topics.filter((t) => t.state === "locked");

  const subtitle = [
    ready.length > 0 ? `${ready.length} ready` : null,
    locked.length > 0 ? `${locked.length} locked` : null,
  ]
    .filter(Boolean)
    .join(" · ");

  return (
    <CollapsibleSection title={course.courseTitle} subtitle={subtitle}>
      <div className="flex flex-col gap-2">
        {ready.length === 0 && (
          <p className="text-sm text-slate-400">Nothing ready yet — finish earlier topics to unlock these.</p>
        )}
        {ready.map((entry) => (
          <PlannerTopicRow key={entry.topic.id} topic={entry.topic} onToggleComplete={onToggle} />
        ))}
        {locked.length > 0 && (
          <p className="mt-1 text-xs text-slate-400">
            + {locked.length} locked, waiting on prerequisites
          </p>
        )}
      </div>
    </CollapsibleSection>
  );
}
