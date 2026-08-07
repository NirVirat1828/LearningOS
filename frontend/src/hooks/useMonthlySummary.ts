import { useQuery } from "@tanstack/react-query";
import { fetchMonthlySummary } from "../api/calendar";

export function useMonthlySummary(year: number, month: number) {
  return useQuery({
    queryKey: ["calendarSummary", year, month],
    queryFn: () => fetchMonthlySummary(year, month),
  });
}
