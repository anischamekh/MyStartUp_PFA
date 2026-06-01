-- Sync auth users/teams into project_db (run after project-service created tables).
CREATE EXTENSION IF NOT EXISTS dblink;

TRUNCATE TABLE team_snapshots, user_snapshots RESTART IDENTITY CASCADE;

INSERT INTO user_snapshots (id, username, full_name, email, role_name, team_id, team_name)
SELECT id, username, full_name, email, role_name, team_id, team_name
FROM dblink(
    'host=127.0.0.1 dbname=auth_db user=postgres password=admin',
    $q$
    SELECT u.id, u.username, u.full_name, u.email, r.name AS role_name, ep.team_id, t.name AS team_name
    FROM users u
    INNER JOIN roles r ON r.id = u.role_id
    LEFT JOIN employee_profiles ep ON ep.user_id = u.id
    LEFT JOIN teams t ON t.id = ep.team_id
    ORDER BY u.id
    $q$
) AS t(id bigint, username varchar, full_name varchar, email varchar, role_name varchar, team_id bigint, team_name varchar);

INSERT INTO team_snapshots (id, name, team_leader_id)
SELECT id, name, team_leader_id
FROM dblink(
    'host=127.0.0.1 dbname=auth_db user=postgres password=admin',
    'SELECT id, name, team_leader_id FROM teams ORDER BY id'
) AS t(id bigint, name varchar, team_leader_id bigint);
