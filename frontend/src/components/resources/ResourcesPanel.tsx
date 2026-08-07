import { useMemo, useState } from "react";
import ResourceCard from "./ResourceCard";
import { ALL_RESOURCE_TYPES, RESOURCE_TYPE_LABELS } from "../../lib/resourceDisplay";
import type { Resource, ResourceType } from "../../types/resources";

export default function ResourcesPanel({ resources }: { resources: Resource[] }) {
  const [activeType, setActiveType] = useState<ResourceType | "ALL">("ALL");
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    return resources.filter((resource) => {
      const matchesType = activeType === "ALL" || resource.type === activeType;
      const matchesSearch = query === "" || resource.title.toLowerCase().includes(query);
      return matchesType && matchesSearch;
    });
  }, [resources, activeType, search]);

  return (
    <div>
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap gap-1.5">
          <FilterChip label="All" active={activeType === "ALL"} onClick={() => setActiveType("ALL")} />
          {ALL_RESOURCE_TYPES.map((type) => (
            <FilterChip
              key={type}
              label={RESOURCE_TYPE_LABELS[type]}
              active={activeType === type}
              onClick={() => setActiveType(type)}
            />
          ))}
        </div>
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search resources..."
          className="w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm focus:border-slate-400 focus:outline-none sm:w-56"
        />
      </div>

      {resources.length === 0 && <p className="mt-6 text-slate-400">No resources for this topic yet.</p>}
      {resources.length > 0 && filtered.length === 0 && (
        <p className="mt-6 text-slate-400">No resources match your filters.</p>
      )}

      {filtered.length > 0 && (
        <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((resource) => (
            <ResourceCard key={resource.id} resource={resource} />
          ))}
        </div>
      )}
    </div>
  );
}

function FilterChip({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
        active ? "bg-slate-900 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"
      }`}
    >
      {label}
    </button>
  );
}
