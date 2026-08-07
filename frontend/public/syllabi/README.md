# Syllabus JSON format

These files are the **content source** for LearningOS in its backendless form. They are
static, committed to the repo, and served as-is by GitHub Pages. They hold *only* the
fixed structure of each learning track — never per-user state.

**Progress is not stored here.** Task status, time spent, completion dates, and today's
plan all live in the browser's `localStorage`, keyed by the stable `id`s below. That
separation is the whole point: regenerating a syllabus file never wipes your progress, and
your progress never bloats a diff.

## Files

- `manifest.json` — the index. Lists every roadmap and the course files it contains.
- `<course>.json` — one learning track: a Course holding Modules holding Topics.

## IDs are slugs, not UUIDs

Every `id` is a human-readable, path-style slug that is **stable across regenerations**
(`system-design/load-balancing/health-checks`). localStorage progress keys off these, so
they must not churn. This is why the static build uses slugs rather than the random UUIDs
the old backend generated.

## Schema

`manifest.json`:

```json
{
  "roadmaps": [
    { "id": "backend", "title": "Backend Engineering",
      "description": "…", "courses": ["system-design", "dsa-30"] }
  ]
}
```

`<course>.json`:

```json
{
  "id": "system-design",
  "title": "System Design Mega-Course",
  "description": "…",
  "modules": [
    {
      "id": "system-design/load-balancing",
      "title": "Load Balancing",
      "description": "…",
      "topics": [
        {
          "id": "system-design/load-balancing/health-checks",
          "title": "Health Checks & Failure Detection",
          "difficulty": "BEGINNER | INTERMEDIATE | ADVANCED",
          "estimatedMinutes": 30,
          "prerequisites": ["system-design/load-balancing/fundamentals"],
          "priority": "ACTIVE | LATER",
          "resources": [
            { "title": "…", "url": "https://…", "type": "OFFICIAL_DOCS | YOUTUBE | BOOK | GITHUB | ARTICLE" }
          ]
        }
      ]
    }
  ]
}
```

- `prerequisites` — topic `id`s (may cross modules) that should be done first. Powers the
  roadmap dependency graph and the planner's ordering. Omit or `[]` for none.
- `priority` — `"ACTIVE"` (default, may be omitted) or `"LATER"`. `"LATER"` marks a backlog /
  pull-as-needed topic: it still shows up in the syllabus browser (tagged "Later") but the
  daily planner (`computeTodaysPlan`) skips it, so it never occupies today's budget on its own.
- `resources` — optional; same five types the old backend used. Omit or `[]` for none.
- `difficulty` / `estimatedMinutes` — used by the topic cards and the AI/rule planner.
