# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`sumika` is a household-budget / personal-finance management SaaS (家計簿管理のSaaSサービス).
Monorepo: `backend/` (Spring Boot API) + `frontend/` (React SPA). Tasks are tracked as GitHub
Issues/milestones (M0 基盤整備 → M1 backend CRUD → M2 frontend UI). License: BSD 3-Clause.

MVP scope is income/expense records (収支記録) CRUD. **Auth is intentionally deferred** — the app
is single-user for now, but DB tables are designed so a `user_id` column can be added later.

## Commands

Local DB (required before running/testing the backend against a real DB):

```bash
docker compose up -d db     # PostgreSQL 16 on localhost:5432 (db/user/pass all "sumika")
docker compose down         # stop (keep data) / down -v to drop the volume
```

Backend (`backend/`, Java 25 + Gradle wrapper; needs Docker for Testcontainers tests):

```bash
./gradlew build                                   # compile + test
./gradlew test                                    # all tests (spins up Testcontainers Postgres)
./gradlew test --tests "com.sumika.SomeTest"      # single test class
./gradlew bootRun                                 # run API on :8080 (Swagger UI at /swagger-ui/index.html)
```

Frontend (`frontend/`, Node 22 + **npm 11** — older npm mis-validates the lockfile):

```bash
npm install        # or: npm ci
npm run dev        # Vite dev server on :5173 (proxies /api → backend :8080)
npm run build      # tsc -b + vite build
npm run lint       # oxlint
npm run format     # prettier --write
npm test           # vitest run
npm run gen:api    # regenerate src/api/schema.d.ts from openapi.json (after API changes)
```

## Architecture

### Backend — hexagonal / ports & adapters (Tom Hombergs, *Get Your Hands Dirty on Clean Architecture*)

Code is organized **by bounded context first, then by layer**. The MVP context is `ledger`
(`com.sumika.ledger`). Dependencies always point inward: `adapter → application → domain`; the
domain has no outward dependencies. Each layer's role is documented in its `package-info.java`.

```
com.sumika
├── common/                              # cross-cutting (exception handling, config, ArchUnit)
└── ledger/
    ├── domain/                          # framework-free rich domain model + value objects
    ├── application/
    │   ├── port/in/                     # use-case interfaces + Command/Query (self-validating)
    │   ├── port/out/                    # interfaces the app needs (implemented by adapters)
    │   └── service/                     # use-case implementations (@Transactional)
    └── adapter/
        ├── in/web/                      # REST controllers + web DTOs
        └── out/persistence/             # JPA entities, Spring Data repos, persistence adapters, mappers
```

Key conventions:
- Keep **three separate models** — domain model, JPA entity, web DTO — and map at each boundary.
- Use cases are interfaces in `port/in`; controllers depend only on those interfaces.
- Schema is owned by **Flyway** (`backend/src/main/resources/db/migration/`); JPA runs with
  `ddl-auto=validate`. Datasource defaults to the compose DB, overridable via `SPRING_DATASOURCE_*`.
- Dependency direction is enforced by **ArchUnit** (`LedgerArchitectureTest` + the
  `HexagonalArchitecture` DSL under `src/test/java/com/sumika/archunit/`).
- Errors are returned as RFC 7807 `ProblemDetail` via a `@RestControllerAdvice` in `common`.

### Frontend — React + Vite + TypeScript

React Router for routing, TanStack Query for server state, axios for HTTP (shared instance in
`src/api/client.ts`). The axios base URL defaults to a relative `/api`; in dev, Vite proxies `/api`
to the backend on `:8080` (same-origin, no CORS), overridable via `VITE_API_BASE_URL`. API types are
generated from the backend's OpenAPI (`openapi.json` → `src/api/schema.d.ts` via `npm run gen:api`).
Providers (`ErrorBoundary` / `QueryClientProvider` / `ToastProvider` / `BrowserRouter`) are wired in
`src/main.tsx`. Directory policy: `api/` (client, types, hooks), `features/` (feature modules with
their screens), `components/` (shared UI). Tests use Vitest + React Testing Library.

## CI & workflow

- 1 Issue = 1 PR. `main` is protected: PR required (0 approvals), no direct push / force-push /
  deletion, conversation resolution required, and the **`ci-success` status check is required**.
- GitHub Actions (`.github/workflows/ci.yml`): a `changes` job (path filter) runs `backend`
  (`./gradlew build`) and `frontend` (`npm ci → lint → build → test`) only when that area changed;
  a final **`ci-success`** job (`needs: [backend, frontend]`, `if: always()`) is the single required
  check — it passes when the area jobs succeed or are skipped, so docs-only / unrelated PRs aren't blocked.
