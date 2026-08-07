# LearningOS

Foundation/architecture only — no feature logic yet. Two independent projects:

- [`/backend`](backend/README.md) — Spring Boot 3, Java 21, layered architecture, H2, sample `/api/health` endpoint
- [`/frontend`](frontend/README.md) — React + TypeScript + Vite + Tailwind, sidebar layout, routing for 7 placeholder pages

## Run both

```bash
# terminal 1
cd backend && ./mvnw spring-boot:run   # http://localhost:8080

# terminal 2
cd frontend && npm install && npm run dev   # http://localhost:5173
```

CORS on the backend (`learningos.cors.allowed-origins` in `backend/src/main/resources/application.yml`)
is already set to allow `http://localhost:5173`, so the frontend's Axios client
(`frontend/src/api/axiosClient.ts`) can call the API directly in development.
