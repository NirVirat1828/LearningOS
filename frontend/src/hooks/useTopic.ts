import { useQuery } from "@tanstack/react-query";
import { fetchTopic } from "../api/syllabus";

export function useTopic(topicId: string | undefined) {
  return useQuery({
    queryKey: ["topic", topicId],
    queryFn: () => fetchTopic(topicId as string),
    enabled: Boolean(topicId),
  });
}
