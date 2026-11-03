# =============================================================================
# start.ps1 — Load .env and run the Student Management System
# Usage: .\start.ps1
# =============================================================================

# Load .env file
$envFile = Join-Path $PSScriptRoot ".env"
if (-Not (Test-Path $envFile)) {
    Write-Error ".env file not found! Copy .env.example to .env and fill in your credentials."
    exit 1
}

Write-Host "Loading environment from .env..." -ForegroundColor Cyan
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line.Split("=", 2)
        if ($parts.Length -eq 2) {
            $key = $parts[0].Trim()
            $val = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
            Write-Host "  Set $key" -ForegroundColor Gray
        }
    }
}

# Set Java
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host ""
Write-Host "Starting Student Management System..." -ForegroundColor Green
Write-Host "  Profile : $env:SPRING_PROFILE" -ForegroundColor Yellow
Write-Host "  DB Host : $env:DB_HOST" -ForegroundColor Yellow
Write-Host "  Server  : http://localhost:$env:SERVER_PORT" -ForegroundColor Yellow
Write-Host ""

.\mvnw.cmd spring-boot:run

