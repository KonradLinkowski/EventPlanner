# Contributing

## Branching

- `main` is always releasable; protected, requires PRs and green CI.
- Work on short-lived feature branches: `feature/<stage>-<short-desc>`,
  `fix/<short-desc>`, `chore/<short-desc>` (e.g. `feature/02-event-crud`).
- Rebase on `main` before opening a PR; squash-merge into `main`.

## Commits (Conventional Commits)

Format: `<type>(<scope>): <short summary>`

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `perf`.
Scope: module/service name, e.g. `event-service`, `frontend`, `infra`, `gateway`.

Examples:
```
feat(event-service): add seat reservation endpoint
fix(booking-service): release seats on payment failure
docs(adr): record monorepo decision
chore(root): configure parent pom plugin versions
```

## Pull requests

- Keep PRs scoped to a single stage/feature from `plan.md` where possible.
- Checklist before requesting review: tests pass locally (`mvn verify`),
  no formatting/lint warnings, OpenAPI/docs updated if the API changed,
  no secrets committed.
