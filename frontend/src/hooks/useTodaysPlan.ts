import { useQuery } from "@tanstack/react-query";
import { computeTodaysPlan } from "../lib/plannerData";

export function useTodaysPlan() {
  return useQuery({
    queryKey: ["todaysPlan"],
    queryFn: computeTodaysPlan,
  });
}
