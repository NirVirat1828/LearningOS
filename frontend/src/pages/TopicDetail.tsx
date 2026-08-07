import { useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { useTopic } from "../hooks/useTopic";
import TopicOverviewPanel from "../components/topic/TopicOverviewPanel";
import ResourcesPanel from "../components/resources/ResourcesPanel";

type Tab = "overview" | "resources";

export default function TopicDetail() {
  // Splat param, not a named one — see the route comment in AppRoutes.tsx:
  // topic ids are slashed slugs, so the whole rest of the path is the id.
  const params = useParams();
  const topicId = params["*"];
  const { data: topic, isLoading, isError } = useTopic(topicId);
  const [activeTab, setActiveTab] = useState<Tab>("overview");

  return (
    <div className="p-6">
      <Link to="/syllabus" className="text-sm text-slate-500 hover:text-slate-700">
        &larr; Back to Syllabus
      </Link>

      {isLoading && <p className="mt-4 text-slate-500">Loading topic...</p>}
      {isError && <p className="mt-4 text-rose-600">Couldn't load this topic.</p>}

      {topic && (
        <>
          <h1 className="mt-2 text-2xl font-semibold text-slate-900">{topic.title}</h1>

          <div className="mt-6 flex gap-1 border-b border-slate-200">
            <TabButton active={activeTab === "overview"} onClick={() => setActiveTab("overview")}>
              Overview
            </TabButton>
            <TabButton active={activeTab === "resources"} onClick={() => setActiveTab("resources")}>
              Resources
            </TabButton>
          </div>

          <div className="mt-6">
            {activeTab === "overview" && <TopicOverviewPanel topic={topic} />}
            {activeTab === "resources" && <ResourcesPanel resources={topic.resources} />}
          </div>
        </>
      )}
    </div>
  );
}

function TabButton({
  active,
  onClick,
  children,
}: {
  active: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-current={active}
      className={`border-b-2 px-3 py-2 text-sm font-medium transition-colors ${
        active ? "border-slate-900 text-slate-900" : "border-transparent text-slate-500 hover:text-slate-700"
      }`}
    >
      {children}
    </button>
  );
}
