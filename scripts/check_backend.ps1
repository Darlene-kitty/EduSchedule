$out = "C:\Users\DELL PRO\Desktop\EduSchedule\scripts\check_backend_output.txt"
$ports = @(8761,8080,8081)
"=== Backend health check $(Get-Date) ===" | Out-File $out -Encoding utf8
foreach($p in $ports){ $t = Test-NetConnection -ComputerName localhost -Port $p; "PORT $p: $($t.TcpTestSucceeded)" | Out-File $out -Encoding utf8 -Append }
try { $r = Invoke-RestMethod -Uri http://localhost:8080/api/profile -Method Get -TimeoutSec 10; "GATEWAY_RESPONSE_OK" | Out-File $out -Append; $r | ConvertTo-Json -Depth 5 | Out-File $out -Append } catch { "GATEWAY_ERROR: $($_.Exception.Message)" | Out-File $out -Append }
