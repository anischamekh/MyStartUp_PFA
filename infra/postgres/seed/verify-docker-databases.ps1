# Shows row counts in the Postgres used by Docker microservices (NOT local pgAdmin PostgreSQL 18).
# Run from repo root: .\infra\postgres\seed\verify-docker-databases.ps1

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
Set-Location $Root

$container = (docker compose ps -q postgres | Select-Object -First 1)
if (-not $container) {
    throw "Docker Postgres is not running."
}

Write-Host "=== Docker Postgres (what microservices use) ===" -ForegroundColor Cyan
Write-Host "Container: $container"
Write-Host ""

$queries = @(
    @{ Db = "auth_db"; Sql = "SELECT 'users' AS t, COUNT(*) FROM users UNION ALL SELECT 'teams', COUNT(*) FROM teams UNION ALL SELECT 'employee_profiles', COUNT(*) FROM employee_profiles;" },
    @{ Db = "hrm_db"; Sql = "SELECT 'user_snapshots' AS t, COUNT(*) FROM user_snapshots UNION ALL SELECT 'leave_requests', COUNT(*) FROM leave_requests UNION ALL SELECT 'payroll', COUNT(*) FROM payroll;" },
    @{ Db = "project_db"; Sql = "SELECT 'user_snapshots' AS t, COUNT(*) FROM user_snapshots UNION ALL SELECT 'projects', COUNT(*) FROM projects UNION ALL SELECT 'tasks', COUNT(*) FROM tasks;" },
    @{ Db = "chatbot_db"; Sql = "SELECT 'chat_messages' AS t, COUNT(*) FROM chat_messages;" }
)

foreach ($q in $queries) {
    Write-Host "--- $($q.Db) ---" -ForegroundColor Yellow
    docker exec -e PGPASSWORD=admin $container psql -U postgres -d $q.Db -c $q.Sql
    Write-Host ""
}

Write-Host "If pgAdmin shows different numbers, you are connected to another server (e.g. PostgreSQL 18 local)." -ForegroundColor Green
Write-Host "In pgAdmin for Docker: host localhost, port 5432, user postgres, password admin, databases auth_db/hrm_db/..." -ForegroundColor Green
