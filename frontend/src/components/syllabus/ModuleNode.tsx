import type { SyllabusModule } from "../../types/syllabus";
import CollapsibleSection from "./CollapsibleSection";
import TopicCard from "./TopicCard";

export default function ModuleNode({ module }: { module: SyllabusModule }) {
  return (
    <CollapsibleSection
      title={module.title}
      subtitle={`${module.topics.length} topic${module.topics.length === 1 ? "" : "s"}`}
    >
      <div className="flex flex-col gap-2">
        {module.topics.map((topic) => (
          <TopicCard key={topic.id} topic={topic} />
        ))}
      </div>
    </CollapsibleSection>
  );
}
