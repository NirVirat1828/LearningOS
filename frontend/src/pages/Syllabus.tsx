import { useRoadmaps } from "../hooks/useRoadmaps";
import SyllabusTree from "../components/syllabus/SyllabusTree";

export default function Syllabus() {
  const { data: roadmaps, isLoading, isError } = useRoadmaps();

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold text-slate-900">Syllabus</h1>
      <p className="mt-2 text-slate-500">Browse your roadmap, courses, modules, and topics.</p>

      <div className="mt-6">
        {isLoading && <p className="text-slate-500">Loading syllabus...</p>}
        {isError && (
          <p className="text-rose-600">Couldn't load the syllabus. Is the backend running?</p>
        )}
        {roadmaps && roadmaps.length === 0 && (
          <p className="text-slate-500">No roadmaps yet.</p>
        )}
        {roadmaps && roadmaps.length > 0 && <SyllabusTree roadmaps={roadmaps} />}
      </div>
    </div>
  );
}
