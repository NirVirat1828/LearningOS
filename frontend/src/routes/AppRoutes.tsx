import { Routes, Route } from "react-router-dom";
import MainLayout from "../components/layout/MainLayout";
import Dashboard from "../pages/Dashboard";
import Syllabus from "../pages/Syllabus";
import TopicDetail from "../pages/TopicDetail";
import Tasks from "../pages/Tasks";
import Backlogs from "../pages/Backlogs";
import Calendar from "../pages/Calendar";
import Settings from "../pages/Settings";

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="/syllabus" element={<Syllabus />} />
        {/* Splat, not :topicId — topic ids are path-style slugs (e.g.
            "dsa-30/sprint-1/group-anagrams") and a named param only ever
            matches one path segment. */}
        <Route path="/syllabus/topics/*" element={<TopicDetail />} />
        <Route path="/tasks" element={<Tasks />} />
        <Route path="/backlogs" element={<Backlogs />} />
        <Route path="/calendar" element={<Calendar />} />
        <Route path="/settings" element={<Settings />} />
      </Route>
    </Routes>
  );
}
