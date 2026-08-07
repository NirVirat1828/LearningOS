import { useDayDetail } from "../../hooks/useDayDetail";
import { formatDateLong } from "../../lib/calendarGrid";

function StatTile({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-md bg-slate-50 px-3 py-2 text-center">
      <div className="text-lg font-semibold text-slate-900">{value}</div>
      <div className="text-[11px] uppercase tracking-wide text-slate-500">{label}</div>
    </div>
  );
}

export default function DayDetailPanel({ date }: { date: string }) {
  const { data, isLoading, isError } = useDayDetail(date);

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="text-sm font-semibold text-slate-900">{formatDateLong(date)}</h2>

      {isLoading && <p className="mt-3 text-sm text-slate-500">Loading...</p>}
      {isError && <p className="mt-3 text-sm text-rose-600">Couldn't load this day.</p>}

      {data && (
        <>
          <div className="mt-3 grid grid-cols-2 gap-2">
            <StatTile label="Topics" value={data.topicsFinished.length} />
            <StatTile label="Time" value={`${data.totalMinutesSpent}m`} />
          </div>

          <div className="mt-4">
            <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500">Topics Finished</h3>
            {data.topicsFinished.length === 0 && (
              <p className="mt-1 text-sm text-slate-400">Nothing completed on this day.</p>
            )}
            <ul className="mt-1 flex flex-col gap-1.5">
              {data.topicsFinished.map((topic) => (
                <li key={topic.id} className="text-sm text-slate-700">
                  {topic.title}
                </li>
              ))}
            </ul>
          </div>
        </>
      )}
    </div>
  );
}
