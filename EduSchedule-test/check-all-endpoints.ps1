$endpoints = @(
    "/api/v1/events",
    "/api/v1/reports",
    "/api/v1/schools",
    "/api/users",
    "/api/v1/schedules",
    "/api/v1/timeslots",
    "/api/v1/groupes",
    "/api/v1/filieres",
    "/api/v1/niveaux",
    "/api/v1/rooms",
    "/api/v1/courses",
    "/api/calendar/events",
    "/api/v1/salles",
    "/api/v1/reservations",
    "/api/v1/notifications",
    "/api/v1/equipment",
    "/api/v1/equipment-types",
    "/api/v1/equipment-usage/dashboard",
    "/api/v1/schedules/upcoming",
    "/api/v1/timetable",
    "/api/v1/exams",
    "/api/v1/categories-ue",
    "/api/teacher-availability",
    "/api/students"
)
foreach ($ep in $endpoints) {
    $req = [System.Net.HttpWebRequest]::Create("http://localhost:8080$ep")
    $req.Headers.Add("Accept-Encoding","identity")
    $req.Method = "GET"
    try {
        $res = $req.GetResponse()
        $code = [int]$res.StatusCode
        $res.Close()
        Write-Output "OK  $code  $ep"
    } catch [System.Net.WebException] {
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
            Write-Output "ERR $code  $ep"
        } else {
            Write-Output "ERR ???  $ep  ($($_.Exception.Message))"
        }
    }
}
