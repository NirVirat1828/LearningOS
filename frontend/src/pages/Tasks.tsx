import { useState } from "react";
import { useTodaysPlan } from "../hooks/useTodaysPlan";
import { useSetTopicComplete } from "../hooks/useSetTopicComplete";
import { useDailyBudget } from "../hooks/useDailyBudget";
import { getDailyMinutes } from "../lib/localPlan";
import DailyProgressBar from "../components/planner/DailyProgressBar";
import PlannerTopicRow from "../components/planner/PlannerTopicRow";
import type { Topic } from "../types/syllabus";

const BUDGET_OPTIONS = [30, 45, 60, 90, 120, 180];

export default function Tasks() {
  const { data: plan, isLoading, isError } = useTodaysPlan();
  const setComplete = useSetTopicComplete();
  const setBudget = useDailyBudget();
  const [budget, setBudgetState] = useState(() => getDailyMinutes());

  const topics = plan?.topics ?? [];
  const pending = topics.filter((t) => t.completionStatus !== "COMPLETED");
  const completed = topics.filter((t) => t.completionStatus === "COMPLETED");
  const pendingMinutes = pending.reduce((sum, t) => sum + t.estimatedMinutes, 0);

  function toggle(topic: Topic) {
    setComplete.mutate({
      topicId: topic.id,
      completed: topic.completionStatus !== "COMPLETED",
      estimatedMinutes: topic.estimatedMinutes,
    });
  }

  function changeBudget(minutes: number) {
    setBudgetState(minutes);
    setBudget.mutate(minutes);
  }

  return (
    <div className="p-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Today's Plan</h1>
          <p className="mt-2 text-slate-500">
            Generated from your syllabus — the next unlocked topics that fit your daily time.
          </p>
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          Daily budget
          <select
            value={budget}
            onChange={(e) => changeBudget(Number(e.target.value))}
            className="rounded-md border border-slate-300 px-2 py-1 text-sm focus:border-slate-400 focus:outline-none"
          >
            {BUDGET_OPTIONS.map((m) => (
              <option key={m} value={m}>
                {m} min
              </option>
            ))}
          </select>
        </label>
      </div>

      {isLoading && <p className="mt-6 text-slate-500">Planning your day...</p>}
      {isError && <p className="mt-6 text-rose-600">Couldn't build today's plan.</p>}

      {plan && (
        <>
          <div className="mt-6">
            <DailyProgressBar completed={completed.length} total={topics.length} />
            <p className="mt-2 text-xs text-slate-400">
              {pendingMinutes} min left · {budget} min/day budget
            </p>
          </div>

          {topics.length === 0 && (
            <p className="mt-6 rounded-lg border border-dashed border-slate-200 p-6 text-center text-sm text-slate-400">
              Nothing to plan right now — finish a topic's prerequisites to unlock the next ones, or
              you're all caught up.
            </p>
          )}

          {topics.length > 0 && (
            <div className="mt-6 grid gap-4 lg:grid-cols-2">
              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <h2 className="text-sm font-semibold text-slate-900">Pending</h2>
                <div className="mt-3 flex flex-col gap-2">
                  {pending.length === 0 && (
                    <p className="text-sm text-slate-400">All done for today — nice work. 🎉</p>
                  )}
                  {pending.map((topic) => (
                    <PlannerTopicRow key={topic.id} topic={topic} onToggleComplete={toggle} />
                  ))}
                </div>
              </div>

              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <h2 className="text-sm font-semibold text-slate-900">Completed</h2>
                <div className="mt-3 flex flex-col gap-2">
                  {completed.length === 0 && (
                    <p className="text-sm text-slate-400">Nothing completed yet today.</p>
                  )}
                  {completed.map((topic) => (
                    <PlannerTopicRow key={topic.id} topic={topic} onToggleComplete={toggle} />
                  ))}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
