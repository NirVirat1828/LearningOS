import type { ReactNode } from "react";

interface StatTileProps {
  label: string;
  value: ReactNode;
  hint?: string;
}

export default function StatTile({ label, value, hint }: StatTileProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white px-4 py-3">
      <div className="text-2xl font-semibold text-slate-900">{value}</div>
      <div className="mt-0.5 text-xs font-medium uppercase tracking-wide text-slate-500">{label}</div>
      {hint && <div className="mt-0.5 text-xs text-slate-400">{hint}</div>}
    </div>
  );
}
