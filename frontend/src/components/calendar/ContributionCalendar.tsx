import { buildMonthGrid } from "../../lib/calendarGrid";
import type { DaySummary } from "../../types/calendar";

const WEEKDAY_LABELS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

type IntensityLevel = 0 | 1 | 2 | 3 | 4;

function intensityLevel(minutesSpent: number): IntensityLevel {
  if (minutesSpent <= 0) return 0;
  if (minutesSpent < 30) return 1;
  if (minutesSpent < 60) return 2;
  if (minutesSpent < 90) return 3;
  return 4;
}

const LEVEL_STYLES: Record<IntensityLevel, string> = {
  0: "bg-slate-100",
  1: "bg-emerald-100",
  2: "bg-emerald-300",
  3: "bg-emerald-500",
  4: "bg-emerald-700",
};

interface ContributionCalendarProps {
  year: number;
  month: number;
  days: DaySummary[];
  selectedDate: string | null;
  onSelectDay: (date: string) => void;
}

export default function ContributionCalendar({
  year,
  month,
  days,
  selectedDate,
  onSelectDay,
}: ContributionCalendarProps) {
  const summaryByDate = new Map(days.map((d) => [d.date, d]));
  const weeks = buildMonthGrid(year, month);
  const todayIso = new Date().toISOString().slice(0, 10);

  return (
    <div>
      <div className="grid grid-cols-7 gap-1.5 text-center text-xs text-slate-400">
        {WEEKDAY_LABELS.map((label) => (
          <span key={label}>{label}</span>
        ))}
      </div>
      <div className="mt-1.5 flex flex-col gap-1.5">
        {weeks.map((week, weekIndex) => (
          <div key={weekIndex} className="grid grid-cols-7 gap-1.5">
            {week.map((date, dayIndex) => {
              if (!date) {
                return <div key={dayIndex} />;
              }
              const summary = summaryByDate.get(date);
              const level = intensityLevel(summary?.minutesSpent ?? 0);
              const isSelected = date === selectedDate;
              const isToday = date === todayIso;
              const dayOfMonth = Number(date.slice(-2));
              return (
                <button
                  key={date}
                  type="button"
                  onClick={() => onSelectDay(date)}
                  title={`${date}: ${summary?.topicsFinished ?? 0} topics, ${summary?.minutesSpent ?? 0} min`}
                  aria-pressed={isSelected}
                  className={`relative aspect-square rounded-md transition-transform hover:scale-105 ${LEVEL_STYLES[level]} ${
                    isSelected ? "ring-2 ring-offset-1 ring-slate-900" : ""
                  } ${isToday ? "outline outline-1 outline-offset-1 outline-slate-400" : ""}`}
                >
                  <span className="sr-only">{date}</span>
                  <span
                    aria-hidden="true"
                    className={`absolute bottom-0.5 right-1 text-[9px] ${
                      level >= 3 ? "text-white/80" : "text-slate-400"
                    }`}
                  >
                    {dayOfMonth}
                  </span>
                </button>
              );
            })}
          </div>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-end gap-1.5 text-xs text-slate-400">
        <span>Less</span>
        {([0, 1, 2, 3, 4] as IntensityLevel[]).map((level) => (
          <span key={level} className={`h-3 w-3 rounded-sm ${LEVEL_STYLES[level]}`} aria-hidden="true" />
        ))}
        <span>More</span>
      </div>
    </div>
  );
}
