-- Sync auth users into hrm_db (run after hrm-service created tables).
CREATE EXTENSION IF NOT EXISTS dblink;

TRUNCATE TABLE employee_hr_data, user_snapshots RESTART IDENTITY CASCADE;

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

INSERT INTO employee_hr_data (user_id, remaining_leave_days)
SELECT user_id, remaining_leave_days
FROM dblink(
    'host=127.0.0.1 dbname=auth_db user=postgres password=admin',
    'SELECT user_id, remaining_leave_days FROM employee_profiles ORDER BY user_id'
) AS t(user_id bigint, remaining_leave_days int);
