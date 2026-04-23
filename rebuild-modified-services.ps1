# ============================================================
#  REBUILD - SERVICES MODIFIES (PowerShell)
#  Usage: .\rebuild-modified-services.ps1
# ============================================================
param(
    [switch]$SkipDocker,
    [switch]$SkipFrontend
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Build-Service($name) {
    Write-Host "`n[BUILD] $name ..." -ForegroundColor Cyan
    Set-Location "$root\$name"
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] Build Maven echoue pour $name" -ForegroundColor Red
        Set-Location $root
        exit 1
    }
    Write-Host "[OK] $name - JAR genere" -ForegroundColor Green
    Set-Location $root
}

# ── Verifications ──────────────────────────────────────────
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "  REBUILD - SERVICES MODIFIES" -ForegroundColor Yellow
Write-Host "============================================================`n"

$mvnOk   = Get-Command mvn   -ErrorAction SilentlyContinue
$nodeOk  = Get-Command node  -ErrorAction SilentlyContinue
$dockerOk = Get-Command docker -ErrorAction SilentlyContinue

if (-not $mvnOk)   { Write-Host "[ERREUR] Maven introuvable" -ForegroundColor Red; exit 1 }
if (-not $nodeOk)  { Write-Host "[ERREUR] Node.js introuvable" -ForegroundColor Red; exit 1 }
Write-Host "[OK] Maven et Node.js disponibles"

# ── 1. Build Maven ─────────────────────────────────────────
$services = @("reservation-service", "notification-service", "course-service", "resource-service")

foreach ($svc in $services) {
    Build-Service $svc
}

# ── 2. Frontend Angular ────────────────────────────────────
if (-not $SkipFrontend) {
    Write-Host "`n[BUILD] frontend-angular - npm install ..." -ForegroundColor Cyan
    Set-Location "$root\frontend-angular"
    npm install --legacy-peer-deps
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] npm install echoue" -ForegroundColor Red
        Set-Location $root
        exit 1
    }
    Write-Host "[OK] Dependances npm installees" -ForegroundColor Green
    Set-Location $root
}

# ── 3. Docker build ────────────────────────────────────────
if (-not $SkipDocker) {
    if ($dockerOk) {
        Write-Host "`n[DOCKER] Reconstruction des images ..." -ForegroundColor Cyan
        docker compose build --no-cache reservation-service notification-service course-service resource-service frontend-angular
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERREUR] docker compose build echoue" -ForegroundColor Red
            exit 1
        }
        Write-Host "[OK] Images Docker reconstruites" -ForegroundColor Green
    } else {
        Write-Host "[SKIP] Docker non disponible - images non reconstruites" -ForegroundColor Yellow
    }
}

Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "  BUILD TERMINE" -ForegroundColor Green
Write-Host "============================================================"
Write-Host "`nPour redemarrer les services modifies :"
Write-Host "  docker compose up -d reservation-service notification-service course-service resource-service frontend-angular"
