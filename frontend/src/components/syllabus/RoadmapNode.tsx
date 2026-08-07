import type { Roadmap } from "../../types/syllabus";
import CollapsibleSection from "./CollapsibleSection";
import CourseNode from "./CourseNode";

export default function RoadmapNode({ roadmap }: { roadmap: Roadmap }) {
  return (
    <CollapsibleSection
      title={roadmap.title}
      subtitle={`${roadmap.courses.length} course${roadmap.courses.length === 1 ? "" : "s"}`}
      defaultExpanded
    >
      <div className="flex flex-col gap-3">
        {roadmap.courses.map((course) => (
          <CourseNode key={course.id} course={course} />
        ))}
      </div>
    </CollapsibleSection>
  );
}
