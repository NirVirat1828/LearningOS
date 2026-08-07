import type { Course } from "../../types/syllabus";
import CollapsibleSection from "./CollapsibleSection";
import ModuleNode from "./ModuleNode";

export default function CourseNode({ course }: { course: Course }) {
  return (
    <CollapsibleSection
      title={course.title}
      subtitle={`${course.modules.length} module${course.modules.length === 1 ? "" : "s"}`}
    >
      <div className="flex flex-col gap-3">
        {course.modules.map((module) => (
          <ModuleNode key={module.id} module={module} />
        ))}
      </div>
    </CollapsibleSection>
  );
}
