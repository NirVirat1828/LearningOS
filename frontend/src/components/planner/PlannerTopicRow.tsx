import { Link } from "react-router-dom";
import type { Topic } from "../../types/syllabus";
import { DIFFICULTY_STYLES, formatEstimatedTime, toTitleCase } from "../../lib/topicDisplay";

interface PlannerTopicRowProps {
  topic: Topic;
  onToggleComplete: (topic: Topic) => void;
}

export default function PlannerTopicRow({ topic, onToggleComplete }: PlannerTopicRowProps) {
  const isCompleted = topic.completionStatus === "COMPLETED";

  return (
    <div className="flex items-center gap-3 rounded-md border border-slate-200 bg-white px-3 py-2">
      <input
        type="checkbox"
        checked={isCompleted}
        onChange={() => onToggleComplete(topic)}
        className="h-4 w-4 shrink-0 rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
        aria-label={`Mark "${topic.title}" as ${isCompleted ? "not done" : "complete"}`}
      />
      <div className="min-w-0 flex-1">
        <Link
          to={`/syllabus/topics/${topic.id}`}
          className={`block truncate text-sm font-medium hover:underline ${
            isCompleted ? "text-slate-400 line-through" : "text-slate-800"
          }`}
        >
          {topic.title}
        </Link>
      </div>
      <span className={`shrink-0 rounded-full px-2 py-0.5 text-[11px] font-medium ${DIFFICULTY_STYLES[topic.difficulty]}`}>
        {toTitleCase(topic.difficulty)}
      </span>
      <span className="shrink-0 text-xs text-slate-400">{formatEstimatedTime(topic.estimatedMinutes)}</span>
    </div>
  );
}
