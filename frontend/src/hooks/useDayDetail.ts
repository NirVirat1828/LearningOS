import { useQuery } from "@tanstack/react-query";
import { fetchDayDetail } from "../api/calendar";

export function useDayDetail(date: string | null) {
  return useQuery({
    queryKey: ["calendarDay", date],
    queryFn: () => fetchDayDetail(date as string),
    enabled: Boolean(date),
  });
}
