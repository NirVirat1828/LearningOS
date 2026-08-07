import { Link } from "react-router-dom";
import type { Topic } from "../../types/syllabus";
import { DIFFICULTY_STYLES, STATUS_STYLES, formatEstimatedTime, toTitleCase } from "../../lib/topicDisplay";

export default function TopicCard({ topic }: { topic: Topic }) {
  return (
    <Link
      to={`/syllabus/topics/${topic.id}`}
      className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 transition-colors hover:border-slate-300 hover:bg-slate-100"
    >
      <span className="font-medium text-slate-800">{topic.title}</span>
      <div className="flex flex-wrap items-center gap-2 text-xs font-medium">
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
    </Link>
  );
}
