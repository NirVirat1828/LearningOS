import type { Topic } from "../../types/syllabus";
import { DIFFICULTY_STYLES, STATUS_STYLES, formatEstimatedTime, toTitleCase } from "../../lib/topicDisplay";
import { useSetTopicComplete } from "../../hooks/useSetTopicComplete";
import LearnWithAiButton from "./LearnWithAiButton";

export default function TopicOverviewPanel({ topic }: { topic: Topic }) {
  const setComplete = useSetTopicComplete();
  const isCompleted = topic.completionStatus === "COMPLETED";

  return (
    <div className="max-w-2xl">
      {topic.description && <p className="text-slate-600">{topic.description}</p>}
      <div className="mt-4 flex flex-wrap gap-2 text-xs font-medium">
        <span className={`rounded-full px-2 py-1 ${DIFFICULTY_STYLES[topic.difficulty]}`}>
          {toTitleCase(topic.difficulty)}
        </span>
        <span className="rounded-full bg-slate-200 px-2 py-1 text-slate-700">
          {formatEstimatedTime(topic.estimatedMinutes)}
        </span>
        <span className={`rounded-full px-2 py-1 ${STATUS_STYLES[topic.completionStatus]}`}>
          {toTitleCase(topic.completionStatus)}
        </span>
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-3">
        <button
          type="button"
          disabled={setComplete.isPending}
          onClick={() =>
            setComplete.mutate({
              topicId: topic.id,
              completed: !isCompleted,
              estimatedMinutes: topic.estimatedMinutes,
            })
          }
          className={`rounded-md px-4 py-2 text-sm font-medium transition-colors disabled:opacity-60 ${
            isCompleted
              ? "border border-slate-300 bg-white text-slate-600 hover:bg-slate-50"
              : "bg-emerald-600 text-white hover:bg-emerald-700"
          }`}
        >
          {isCompleted ? "Mark as not done" : "Mark as complete"}
        </button>
        <LearnWithAiButton topic={topic} />
      </div>
      <p className="mt-2 text-xs text-slate-400">
        “Learn with AI” copies a ready-made teaching prompt — paste it into Claude or ChatGPT.
      </p>
    </div>
  );
}
