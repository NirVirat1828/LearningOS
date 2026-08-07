import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";

/**
 * Shared shell rendered around every route: sidebar on the left, the active
 * page's content in the main area via <Outlet />.
 */
export default function MainLayout() {
  return (
    <div className="flex min-h-screen bg-slate-50">
      <Sidebar />
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
