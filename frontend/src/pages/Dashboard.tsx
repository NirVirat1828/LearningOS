import { Link } from "react-router-dom";
import { useProgressStats } from "../hooks/useProgressStats";
import StatTile from "../components/StatTile";
import { formatEstimatedTime } from "../lib/topicDisplay";
import type { CourseSummary } from "../lib/progressStats";

function percent(completed: number, total: number): number {
  return total > 0 ? Math.round((completed / total) * 100) : 0;
}

export default function Dashboard() {
  const { data: stats, isLoading, isError } = useProgressStats();

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold text-slate-900">Dashboard</h1>
      <p className="mt-2 text-slate-500">Your learning at a glance.</p>

      {isLoading && <p className="mt-6 text-slate-500">Loading your progress...</p>}
      {isError && <p className="mt-6 text-rose-600">Couldn't load your progress.</p>}

      {stats && (
        <>
          <div className="mt-6 grid grid-cols-2 gap-3 lg:grid-cols-4">
            <StatTile
              label="Topics done"
              value={`${stats.completed}/${stats.totalTopics}`}
              hint={`${percent(stats.completed, stats.totalTopics)}% complete`}
            />
            <StatTile label="Time invested" value={formatEstimatedTime(stats.minutesInvested) || "0 min"} />
            <StatTile label="Active days" value={stats.activeDays} />
            <StatTile label="Ready now" value={stats.ready} hint="unlocked topics" />
          </div>

          <div className="mt-6 rounded-lg border border-slate-200 bg-white p-4">
            <div className="flex items-center justify-between text-sm">
              <span className="font-medium text-slate-900">Overall progress</span>
              <span className="text-slate-500">{percent(stats.completed, stats.totalTopics)}%</span>
            </div>
            <div className="mt-2 h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className="h-full rounded-full bg-emerald-500 transition-all"
                style={{ width: `${percent(stats.completed, stats.totalTopics)}%` }}
              />
            </div>
          </div>

          <div className="mt-6 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-900">Courses</h2>
            <Link to="/tasks" className="text-sm font-medium text-emerald-700 hover:underline">
              Go to Today's Plan &rarr;
            </Link>
          </div>

          <div className="mt-3 flex flex-col gap-2">
            {stats.courses.map((course) => (
              <CourseProgressRow key={course.courseId} course={course} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function CourseProgressRow({ course }: { course: CourseSummary }) {
  const pct = percent(course.completed, course.total);
  return (
    <div className="rounded-lg border border-slate-200 bg-white px-4 py-3">
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="min-w-0 truncate font-medium text-slate-800">{course.courseTitle}</span>
        <span className="shrink-0 text-slate-500">
          {course.completed}/{course.total}
        </span>
      </div>
      <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
        <div className="h-full rounded-full bg-emerald-500 transition-all" style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}
