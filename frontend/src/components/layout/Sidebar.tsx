import { NavLink } from "react-router-dom";

const NAV_ITEMS = [
  { to: "/", label: "Dashboard" },
  { to: "/syllabus", label: "Syllabus" },
  { to: "/tasks", label: "Tasks" },
  { to: "/backlogs", label: "Backlogs" },
  { to: "/calendar", label: "Calendar" },
  { to: "/settings", label: "Settings" },
];

export default function Sidebar() {
  return (
    <aside className="flex w-60 shrink-0 flex-col border-r border-slate-200 bg-white">
      <div className="px-4 py-5">
        <span className="text-lg font-semibold text-slate-900">LearningOS</span>
      </div>
      <nav className="flex flex-col gap-1 px-2">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) =>
              `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-slate-900 text-white"
                  : "text-slate-600 hover:bg-slate-100"
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
