# PostgreSQL bootstrap (microservices)

## Files

| File | Purpose |
|------|---------|
| `init-databases.sql` | Creates `auth_db`, `hrm_db`, `project_db`, `chatbot_db` (Docker first start) |
| `schema/README.md` | Tables are created by Hibernate when services start |
| `seed/01-auth-minimal.sql` | ADMIN, HR, MANAGER, EMPLOYEE users (password: `password`) |
| `seed/02-sync-hrm-snapshots.sql` | Copies users into `hrm_db.user_snapshots` |
| `seed/03-sync-project-snapshots.sql` | Copies users/teams into `project_db` |
| `seed/apply-seed.ps1` | Runs all seeds against Docker Postgres |
| `seed/verify-docker-databases.ps1` | Row counts for sanity check |

## Quick start

```powershell
docker compose up -d postgres auth-service hrm-service project-service chatbot-service
.\infra\postgres\seed\apply-seed.ps1
```

Use **Docker Postgres** in pgAdmin (`localhost:5432`, user `postgres`, password `admin`), not a separate local PostgreSQL 18 instance unless you migrate data yourself.
