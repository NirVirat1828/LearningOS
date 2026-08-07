import { useMutation, useQueryClient } from "@tanstack/react-query";
import { completeTopic, resetTopic } from "../lib/localProgress";

interface Vars {
  topicId: string;
  completed: boolean;
  estimatedMinutes: number;
}

/**
 * Writes topic completion to localStorage, then invalidates every query that
 * reads progress — the syllabus tree (["roadmaps"]), this topic (["topic"]),
 * and the calendar (["calendarSummary"], ["calendarDay"]) — so a completed
 * topic shows up everywhere without a page reload.
 */
export function useSetTopicComplete() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ topicId, completed, estimatedMinutes }: Vars) => {
      if (completed) {
        completeTopic(topicId, estimatedMinutes);
      } else {
        resetTopic(topicId);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["roadmaps"] });
      queryClient.invalidateQueries({ queryKey: ["topic"] });
      queryClient.invalidateQueries({ queryKey: ["todaysPlan"] });
      queryClient.invalidateQueries({ queryKey: ["progressStats"] });
      queryClient.invalidateQueries({ queryKey: ["calendarSummary"] });
      queryClient.invalidateQueries({ queryKey: ["calendarDay"] });
    },
  });
}
