import type { Roadmap } from "../../types/syllabus";
import RoadmapNode from "./RoadmapNode";

export default function SyllabusTree({ roadmaps }: { roadmaps: Roadmap[] }) {
  return (
    <div className="flex flex-col gap-4">
      {roadmaps.map((roadmap) => (
        <RoadmapNode key={roadmap.id} roadmap={roadmap} />
      ))}
    </div>
  );
}
