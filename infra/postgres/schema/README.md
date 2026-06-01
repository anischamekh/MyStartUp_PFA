# Database schema (tables)

Table definitions are **not** maintained as SQL in this repository.

Each microservice creates and updates its schema via Hibernate on startup:

- `SPRING_JPA_HIBERNATE_DDL_AUTO=update` (see `docker-compose.yml`)

**Bootstrap order**

1. `init-databases.sql` — creates empty databases
2. Start `auth-service`, `hrm-service`, `project-service`, `chatbot-service` — tables are created
3. `seed/apply-seed.ps1` — inserts minimal users and syncs snapshots
