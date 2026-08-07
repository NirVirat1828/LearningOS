# LearningOS Frontend

React + TypeScript + Vite + Tailwind CSS frontend for LearningOS. Architecture only — routing and
layout are wired up, pages are placeholders with no feature logic yet.

## Run

```bash
cp .env.example .env   # first time only
npm install
npm run dev
```

Starts on `http://localhost:5173`. The backend is expected on `http://localhost:8080` (see
`VITE_API_BASE_URL` in `.env`).

## Structure

- `src/api` — Axios client setup (`axiosClient.ts`), the single place HTTP config lives
- `src/components/layout` — `Sidebar` and `MainLayout` (sidebar + `<Outlet />`), shared across all pages
- `src/pages` — one placeholder component per route: Dashboard, Syllabus, Roadmap, Tasks, Backlogs, Calendar, Settings
- `src/routes` — `AppRoutes.tsx`, the single source of truth mapping URLs to pages
- `src/index.css` — Tailwind entry point (`@import "tailwindcss"`)
