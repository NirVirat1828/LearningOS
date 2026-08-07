import { useState } from "react";
import { useMonthlySummary } from "../hooks/useMonthlySummary";
import ContributionCalendar from "../components/calendar/ContributionCalendar";
import DayDetailPanel from "../components/calendar/DayDetailPanel";
import { MONTH_NAMES } from "../lib/calendarGrid";

export default function Calendar() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const { data, isLoading, isError } = useMonthlySummary(year, month);

  function goToPreviousMonth() {
    if (month === 1) {
      setYear((y) => y - 1);
      setMonth(12);
    } else {
      setMonth((m) => m - 1);
    }
    setSelectedDate(null);
  }

  function goToNextMonth() {
    if (month === 12) {
      setYear((y) => y + 1);
      setMonth(1);
    } else {
      setMonth((m) => m + 1);
    }
    setSelectedDate(null);
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold text-slate-900">Calendar</h1>
      <p className="mt-2 text-slate-500">Your learning activity, day by day.</p>

      <div className="mt-6 flex items-center justify-center gap-4">
        <button
          type="button"
          onClick={goToPreviousMonth}
          aria-label="Previous month"
          className="rounded-md px-2 py-1 text-slate-500 hover:bg-slate-100 hover:text-slate-700"
        >
          &larr;
        </button>
        <span className="w-40 text-center font-medium text-slate-800">
          {MONTH_NAMES[month - 1]} {year}
        </span>
        <button
          type="button"
          onClick={goToNextMonth}
          aria-label="Next month"
          className="rounded-md px-2 py-1 text-slate-500 hover:bg-slate-100 hover:text-slate-700"
        >
          &rarr;
        </button>
      </div>

      <div className="mt-4 grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          {isLoading && <p className="text-slate-500">Loading calendar...</p>}
          {isError && <p className="text-rose-600">Couldn't load the calendar. Is the backend running?</p>}
          {data && (
            <ContributionCalendar
              year={year}
              month={month}
              days={data.days}
              selectedDate={selectedDate}
              onSelectDay={setSelectedDate}
            />
          )}
        </div>

        {selectedDate ? (
          <DayDetailPanel date={selectedDate} />
        ) : (
          <div className="flex items-center justify-center rounded-lg border border-dashed border-slate-200 p-4 text-center text-sm text-slate-400">
            Click a day to see what you worked on.
          </div>
        )}
      </div>
    </div>
  );
}
