import { useState, type ReactNode } from "react";

interface CollapsibleSectionProps {
  title: string;
  subtitle?: string;
  defaultExpanded?: boolean;
  children: ReactNode;
}

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      aria-hidden="true"
      className={`h-4 w-4 shrink-0 text-slate-400 transition-transform ${expanded ? "rotate-90" : ""}`}
    >
      <path d="M7 5l6 5-6 5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export default function CollapsibleSection({
  title,
  subtitle,
  defaultExpanded = false,
  children,
}: CollapsibleSectionProps) {
  const [expanded, setExpanded] = useState(defaultExpanded);

  return (
    <div className="rounded-lg border border-slate-200 bg-white">
      <button
        type="button"
        onClick={() => setExpanded((prev) => !prev)}
        aria-expanded={expanded}
        className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left"
      >
        <span className="flex items-center gap-2">
          <ChevronIcon expanded={expanded} />
          <span className="font-medium text-slate-900">{title}</span>
        </span>
        {subtitle && <span className="text-sm text-slate-500">{subtitle}</span>}
      </button>
      {expanded && <div className="border-t border-slate-200 px-4 py-3">{children}</div>}
    </div>
  );
}
