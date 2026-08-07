import { useMutation, useQueryClient } from "@tanstack/react-query";
import { clearStoredPlan, setDailyMinutes, todayIso } from "../lib/localPlan";

/**
 * Changing the daily budget clears today's stored plan and invalidates it, so
 * the planner recomputes today's list at the new size (adding or dropping
 * topics to fit) rather than leaving the old, differently-sized plan in place.
 */
export function useDailyBudget() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (minutes: number) => {
      setDailyMinutes(minutes);
      clearStoredPlan(todayIso());
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["todaysPlan"] });
    },
  });
}
