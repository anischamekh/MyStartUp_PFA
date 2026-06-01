-- Minimal seed data (roles). Users are created in DataInitializer.
INSERT INTO roles (name) VALUES ('EMPLOYEE') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('TEAM_LEADER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('MANAGER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('HR') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;

