import { Link } from "react-router-dom";
import type { Topic } from "../../types/syllabus";
import { DIFFICULTY_STYLES, formatEstimatedTime, toTitleCase } from "../../lib/topicDisplay";
import LearnWithAiButton from "../topic/LearnWithAiButton";

interface PlannerTopicRowProps {
  topic: Topic;
  /** Omitted in the read-only "review" variant, where the topic is already done. */
  onToggleComplete?: (topic: Topic) => void;
  /** "review" hides the checkbox and renders a revisit link instead of a to-do. */
  variant?: "default" | "review";
}

export default function PlannerTopicRow({
  topic,
  onToggleComplete,
  variant = "default",
}: PlannerTopicRowProps) {
  const isReview = variant === "review";
  const isCompleted = topic.completionStatus === "COMPLETED";

  return (
    <div className="flex items-center gap-3 rounded-md border border-slate-200 bg-white px-3 py-2">
      {isReview ? (
        <span aria-hidden className="shrink-0 text-slate-400" title="Revisit for a quick recall pass">
          ↻
        </span>
      ) : (
        <input
          type="checkbox"
          checked={isCompleted}
          onChange={() => onToggleComplete?.(topic)}
          className="h-4 w-4 shrink-0 rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
          aria-label={`Mark "${topic.title}" as ${isCompleted ? "not done" : "complete"}`}
        />
      )}
      <div className="min-w-0 flex-1">
        <Link
          to={`/syllabus/topics/${topic.id}`}
          className={`block truncate text-sm font-medium hover:underline ${
            !isReview && isCompleted ? "text-slate-400 line-through" : "text-slate-800"
          }`}
        >
          {topic.title}
        </Link>
      </div>
      <span className={`shrink-0 rounded-full px-2 py-0.5 text-[11px] font-medium ${DIFFICULTY_STYLES[topic.difficulty]}`}>
        {toTitleCase(topic.difficulty)}
      </span>
      <span className="shrink-0 text-xs text-slate-400">{formatEstimatedTime(topic.estimatedMinutes)}</span>
      <LearnWithAiButton topic={topic} variant="icon" />
    </div>
  );
}
