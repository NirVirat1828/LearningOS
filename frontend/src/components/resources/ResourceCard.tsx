import type { Resource } from "../../types/resources";
import { RESOURCE_TYPE_LABELS, RESOURCE_TYPE_STYLES } from "../../lib/resourceDisplay";

function hostnameOf(url: string | null): string | null {
  if (!url) return null;
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return null;
  }
}

export default function ResourceCard({ resource }: { resource: Resource }) {
  const host = hostnameOf(resource.url);

  return (
    <a
      href={resource.url ?? undefined}
      target="_blank"
      rel="noopener noreferrer"
      className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-white p-4 transition-shadow hover:shadow-md"
    >
      <span className={`w-fit rounded-full px-2 py-0.5 text-[11px] font-medium ${RESOURCE_TYPE_STYLES[resource.type]}`}>
        {RESOURCE_TYPE_LABELS[resource.type]}
      </span>
      <span className="text-sm font-medium text-slate-900">{resource.title}</span>
      {host && <span className="text-xs text-slate-400">{host}</span>}
    </a>
  );
}
