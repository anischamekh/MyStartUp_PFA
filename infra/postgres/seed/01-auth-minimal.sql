-- Minimal auth_db seed: ADMIN, HR, MANAGER, EMPLOYEE (password: password)
-- BCrypt: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

INSERT INTO roles (id, name, created_at, updated_at) VALUES
  (1, 'EMPLOYEE', NOW(), NOW()),
  (2, 'TEAM_LEADER', NOW(), NOW()),
  (3, 'MANAGER', NOW(), NOW()),
  (4, 'HR', NOW(), NOW()),
  (5, 'ADMIN', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, password, full_name, email, role_id, created_at, updated_at) VALUES
  (1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Admin', 'admin@mystartup.local', 5, NOW(), NOW()),
  (2, 'hr', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'HR User', 'hr@mystartup.local', 4, NOW(), NOW()),
  (3, 'manager', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Project Manager', 'manager@mystartup.local', 3, NOW(), NOW()),
  (4, 'employee', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Employee User', 'employee@mystartup.local', 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO teams (id, name, speciality, team_leader_id, created_at, updated_at) VALUES
  (1, 'Product Squad', 'FRONTEND', 3, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_profiles (id, user_id, team_id, remaining_leave_days, job_title, phone, address, speciality, hire_date, experience_level, salary, created_at, updated_at) VALUES
  (1, 4, 1, 25, 'Developer', '+21600000004', 'Tunis', 'FRONTEND', '2023-06-01', 'MID', 3200.00, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('roles', 'id'), (SELECT COALESCE(MAX(id), 1) FROM roles), true);
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT COALESCE(MAX(id), 1) FROM users), true);
SELECT setval(pg_get_serial_sequence('teams', 'id'), (SELECT COALESCE(MAX(id), 1) FROM teams), true);
SELECT setval(pg_get_serial_sequence('employee_profiles', 'id'), (SELECT COALESCE(MAX(id), 1) FROM employee_profiles), true);
