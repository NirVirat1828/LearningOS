import { useQuery } from "@tanstack/react-query";
import { computeProgressStats } from "../lib/progressStats";

export function useProgressStats() {
  return useQuery({
    queryKey: ["progressStats"],
    queryFn: computeProgressStats,
  });
}
