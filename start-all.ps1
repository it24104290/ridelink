# Start all 4 RideLink microservices in separate PowerShell windows
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Starting RideLink Microservices...    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$baseDir = $PSScriptRoot

Write-Host "[1/4] Launching Account Service (Port 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\account-service'; .\mvnw.cmd spring-boot:run"

Write-Host "[2/4] Launching Driver & Vehicle Service (Port 8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\driver-vehicle-service'; .\mvnw.cmd spring-boot:run"

Write-Host "[3/4] Launching Fare & Payment Service (Port 8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\fare-payment-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 3

Write-Host "[4/4] Launching Ride Management Service (Port 8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\ride-management-service'; .\mvnw.cmd spring-boot:run"

Write-Host ""
Write-Host "All 4 microservices have been launched in separate terminal windows!" -ForegroundColor Green
Write-Host "Swagger UI Dashboards:" -ForegroundColor Cyan
Write-Host "  - Account Service:        http://localhost:8081/swagger-ui.html"
Write-Host "  - Driver & Vehicle:       http://localhost:8082/swagger-ui.html"
Write-Host "  - Ride Management:        http://localhost:8083/swagger-ui.html"
Write-Host "  - Fare & Payment:         http://localhost:8084/swagger-ui.html"
