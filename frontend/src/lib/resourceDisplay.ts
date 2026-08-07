import type { ResourceType } from "../types/resources";

export const ALL_RESOURCE_TYPES: ResourceType[] = ["OFFICIAL_DOCS", "YOUTUBE", "BOOK", "GITHUB", "ARTICLE"];

export const RESOURCE_TYPE_LABELS: Record<ResourceType, string> = {
  OFFICIAL_DOCS: "Official Docs",
  YOUTUBE: "YouTube",
  BOOK: "Book",
  GITHUB: "GitHub",
  ARTICLE: "Article",
};

export const RESOURCE_TYPE_STYLES: Record<ResourceType, string> = {
  OFFICIAL_DOCS: "bg-indigo-100 text-indigo-700",
  YOUTUBE: "bg-red-100 text-red-700",
  BOOK: "bg-amber-100 text-amber-700",
  GITHUB: "bg-slate-200 text-slate-800",
  ARTICLE: "bg-blue-100 text-blue-700",
};
