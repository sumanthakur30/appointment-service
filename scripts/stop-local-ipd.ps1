$ErrorActionPreference = "Continue"
$ipdRepo = Split-Path -Parent $PSScriptRoot
$umbrella = Split-Path -Parent $ipdRepo
$overlay = Join-Path $ipdRepo "deploy\docker-compose.ipd.yml"
Set-Location $umbrella
$env:COMPOSE_ENV_FILES = ".env.local"
& docker compose -f docker-compose.yml -f $overlay --profile ipd stop ipd-service accommodation-service
Write-Host "Local IPD containers stopped. Restart with ipd-service\scripts\start-local-ipd.ps1"
