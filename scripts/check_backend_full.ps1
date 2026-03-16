$out = 'C:\Users\DELL PRO\Desktop\EduSchedule\scripts\check_backend_full_output.txt'
"=== Backend full check $(Get-Date) ===" | Out-File $out -Encoding utf8
$ports = @(8761,8080,8081,8082,8083,8084,8085,8086,8087)
foreach($p in $ports){ $t = Test-NetConnection -ComputerName 'localhost' -Port $p; "PORT ${p}: $($t.TcpTestSucceeded)" | Out-File $out -Append }
# Eureka
try { $e = Invoke-RestMethod -Uri 'http://localhost:8761/' -Method Get -TimeoutSec 5; 'EUREKA_OK' | Out-File $out -Append } catch { 'EUREKA_NOT_OK' | Out-File $out -Append }
# Gateway health
try { $g = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get -TimeoutSec 5; "GATEWAY_HEALTH: $($g.status)" | Out-File $out -Append } catch { 'GATEWAY_HEALTH_ERR' | Out-File $out -Append }
# User service health
try { $u = Invoke-RestMethod -Uri 'http://localhost:8081/actuator/health' -Method Get -TimeoutSec 5; "USER_HEALTH: $($u.status)" | Out-File $out -Append } catch { 'USER_HEALTH_ERR' | Out-File $out -Append }
# Try profile via gateway
try { $p = Invoke-RestMethod -Uri 'http://localhost:8080/api/profile' -Method Get -TimeoutSec 10; "PROFILE_OK" | Out-File $out -Append; $p | ConvertTo-Json -Depth 5 | Out-File $out -Append } catch { "PROFILE_ERR: $($_.Exception.Message)" | Out-File $out -Append }
