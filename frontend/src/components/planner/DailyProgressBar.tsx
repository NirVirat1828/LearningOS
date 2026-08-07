interface DailyProgressBarProps {
  completed: number;
  total: number;
}

export default function DailyProgressBar({ completed, total }: DailyProgressBarProps) {
  const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <div>
      <div className="flex items-center justify-between text-sm text-slate-600">
        <span>Today's progress</span>
        <span>
          {completed} of {total} completed
        </span>
      </div>
      <div className="mt-1.5 h-2 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className="h-full rounded-full bg-emerald-500 transition-all"
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={percentage}
          aria-valuemin={0}
          aria-valuemax={100}
        />
      </div>
    </div>
  );
}
