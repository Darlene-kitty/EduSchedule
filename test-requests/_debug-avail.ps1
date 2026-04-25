$loginResp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$TOKEN = $loginResp.token
Write-Host "Token OK: $($TOKEN.Substring(0,30))..."

$body = '{"teacherId":1,"schoolId":1,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","recurring":true,"priority":1,"notes":"test"}'

try {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/teacher-availability" `
        -Method POST `
        -Headers @{ Authorization = "Bearer $TOKEN"; "Content-Type" = "application/json" } `
        -Body $body
    Write-Host "SUCCESS:" ($r | ConvertTo-Json -Depth 3)
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    try {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errBody = $reader.ReadToEnd()
    } catch { $errBody = "(impossible de lire le corps)" }
    Write-Host "FAIL HTTP $status"
    Write-Host "BODY: $errBody"
}
