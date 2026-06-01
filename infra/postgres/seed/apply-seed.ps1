# Applies minimal SQL seed to Docker Postgres (auth + HRM/Project snapshots).
# Prerequisites:
#   docker compose up -d postgres auth-service hrm-service project-service
# Run from repo root: .\infra\postgres\seed\apply-seed.ps1

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
Set-Location $Root

$container = (docker compose ps -q postgres | Select-Object -First 1)
if (-not $container) {
    throw "Docker Postgres is not running. Run: docker compose up -d postgres"
}

function Apply-Sql {
    param([string]$Database, [string]$File)
    $leaf = Split-Path $File -Leaf
    $remote = "/tmp/seed-$leaf"
    Write-Host ">> $Database : $leaf"
    docker cp $File "${container}:${remote}"
    docker exec -e PGPASSWORD=admin $container psql -U postgres -d $Database -v ON_ERROR_STOP=1 -f $remote
    if ($LASTEXITCODE -ne 0) { throw "SQL failed: $leaf" }
}

$userCount = (docker exec -e PGPASSWORD=admin $container psql -U postgres -d auth_db -tAc "SELECT COUNT(*) FROM users" 2>$null)
if ($userCount -and $userCount.Trim() -ne "0") {
    Write-Host "auth_db already has users - re-applying minimal seed (idempotent inserts)."
}

Write-Host "=== auth_db ===" -ForegroundColor Cyan
Apply-Sql -Database "auth_db" -File (Join-Path $PSScriptRoot "01-auth-minimal.sql")

Write-Host "=== hrm_db snapshots ===" -ForegroundColor Cyan
Apply-Sql -Database "hrm_db" -File (Join-Path $PSScriptRoot "02-sync-hrm-snapshots.sql")

Write-Host "=== project_db snapshots ===" -ForegroundColor Cyan
Apply-Sql -Database "project_db" -File (Join-Path $PSScriptRoot "03-sync-project-snapshots.sql")

Write-Host "=== Kafka user sync ===" -ForegroundColor Cyan
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
docker compose restart auth-service 2>&1 | Out-Null
$ErrorActionPreference = $prevEap

Write-Host ""
& (Join-Path $PSScriptRoot "verify-docker-databases.ps1")
Write-Host ""
Write-Host "Logins (password: password): admin, hr, manager, employee" -ForegroundColor Green
