# Check if reservation-service is in Eureka
$wc = [System.Net.WebClient]::new()
$wc.Headers.Add("Accept-Encoding","identity")
$wc.Headers.Add("Accept","application/json")
try {
    $resp = $wc.DownloadString("http://localhost:8761/eureka/apps/RESERVATION-SERVICE")
    Write-Output "Eureka entry: $resp"
} catch {
    Write-Output "Not found in Eureka: $($_.Exception.Message)"
}

# Try hitting reservation-service directly
$ports = @(8086, 8087, 8088, 8093, 8094, 8096)
foreach ($port in $ports) {
    $req = [System.Net.HttpWebRequest]::Create("http://localhost:$port/actuator/health")
    $req.Headers.Add("Accept-Encoding","identity")
    $req.Method = "GET"
    $req.Timeout = 2000
    try {
        $res = $req.GetResponse()
        $stream = $res.GetResponseStream()
        $reader = [System.IO.StreamReader]::new($stream)
        $body = $reader.ReadToEnd()
        Write-Output "Port $port -> UP: $body"
        $res.Close()
    } catch {
        Write-Output "Port $port -> DOWN"
    }
}
