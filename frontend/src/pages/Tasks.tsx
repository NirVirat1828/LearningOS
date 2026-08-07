import { useState } from "react";
import { useTodaysPlan } from "../hooks/useTodaysPlan";
import { useSetTopicComplete } from "../hooks/useSetTopicComplete";
import { useDailyBudget } from "../hooks/useDailyBudget";
import { getDailyMinutes } from "../lib/localPlan";
import type { PlanSectionKind } from "../lib/plannerData";
import DailyProgressBar from "../components/planner/DailyProgressBar";
import PlannerTopicRow from "../components/planner/PlannerTopicRow";
import type { Topic } from "../types/syllabus";

const BUDGET_OPTIONS = [30, 45, 60, 90, 120, 180];

const SECTION_META: Record<PlanSectionKind, { label: string; chip: string; card: string }> = {
  FOCUS: {
    label: "Focus",
    chip: "bg-indigo-100 text-indigo-700",
    card: "border-indigo-200 bg-indigo-50/40",
  },
  VARIETY: {
    label: "Change of pace",
    chip: "bg-amber-100 text-amber-700",
    card: "border-amber-200 bg-amber-50/40",
  },
  REVIEW: {
    label: "Review",
    chip: "bg-violet-100 text-violet-700",
    card: "border-violet-200 bg-violet-50/40",
  },
};

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
            A deep-focus block plus a spark of variety — sequenced so you build real depth without
            getting lost.
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
              {pendingMinutes} min of new learning left · {budget} min/day budget
            </p>
          </div>

          {plan.sections.length === 0 && (
            <p className="mt-6 rounded-lg border border-dashed border-slate-200 p-6 text-center text-sm text-slate-400">
              Nothing to plan right now — finish a topic's prerequisites to unlock the next ones, or
              you're all caught up.
            </p>
          )}

          <div className="mt-6 flex flex-col gap-4">
            {plan.sections.map((section) => {
              const meta = SECTION_META[section.kind];
              const isReview = section.kind === "REVIEW";
              return (
                <section
                  key={`${section.kind}:${section.title}`}
                  className={`rounded-lg border p-4 ${meta.card}`}
                >
                  <div className="flex flex-wrap items-center gap-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide ${meta.chip}`}
                    >
                      {meta.label}
                    </span>
                    <h2 className="text-sm font-semibold text-slate-900">{section.title}</h2>
                    {section.courseTitle && (
                      <span className="text-xs text-slate-400">· {section.courseTitle}</span>
                    )}
                  </div>
                  <p className="mt-2 text-xs italic text-slate-500">{section.reason}</p>
                  <div className="mt-3 flex flex-col gap-2">
                    {section.topics.map((topic) => (
                      <PlannerTopicRow
                        key={topic.id}
                        topic={topic}
                        variant={isReview ? "review" : "default"}
                        onToggleComplete={isReview ? undefined : toggle}
                      />
                    ))}
                  </div>
                </section>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}
