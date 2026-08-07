export type ResourceType = "OFFICIAL_DOCS" | "YOUTUBE" | "BOOK" | "GITHUB" | "ARTICLE";

export interface Resource {
  id: string;
  title: string;
  url: string | null;
  type: ResourceType;
}
