import { useQuery } from "@tanstack/react-query";
import { fetchRoadmaps } from "../api/syllabus";

export function useRoadmaps() {
  return useQuery({
    queryKey: ["roadmaps"],
    queryFn: fetchRoadmaps,
  });
}
