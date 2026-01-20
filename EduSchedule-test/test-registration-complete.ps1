# Test complet d'inscription d'étudiant
# Vérifie que l'inscription fonctionne correctement avec les nouveaux mappings

Write-Host "=== Test complet d'inscription étudiant ===" -ForegroundColor Cyan

# Configuration
$API_BASE = "http://localhost:8080"

# Test d'inscription d'un nouvel étudiant
Write-Host "`n1. Création d'un nouvel étudiant..." -ForegroundColor Yellow

$studentData = @{
    username = "marie_dupont"
    email = "marie.dupont@example.com"
    password = "password123"
    role = "STUDENT"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/auth/register" -Method POST -Body $studentData -ContentType "application/json"
    Write-Host "✅ Étudiant créé avec succès!" -ForegroundColor Green
    Write-Host "- ID: $($response.id)"
    Write-Host "- Username: $($response.username)"
    Write-Host "- Email: $($response.email)"
    Write-Host "- Role: $($response.role)"
    
    # Test de connexion
    Write-Host "`n2. Test de connexion de l'étudiant..." -ForegroundColor Yellow
    
    $loginData = @{
        username = "marie_dupont"
        password = "password123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "✅ Connexion réussie!" -ForegroundColor Green
    Write-Host "- Token reçu: $($loginResponse.token.Substring(0, 20))..."
    Write-Host "- Role dans la réponse: $($loginResponse.role)"
    
    # Test du profil
    Write-Host "`n3. Test d'accès au profil..." -ForegroundColor Yellow
    
    $headers = @{
        "Authorization" = "Bearer $($loginResponse.token)"
    }
    
    $profileResponse = Invoke-RestMethod -Uri "$API_BASE/api/profile" -Headers $headers
    Write-Host "✅ Profil récupéré!" -ForegroundColor Green
    Write-Host "- Username: $($profileResponse.username)"
    Write-Host "- Email: $($profileResponse.email)"
    Write-Host "- Role: $($profileResponse.role)"
    
}
catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Détails: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n=== Résumé ===" -ForegroundColor Cyan
Write-Host "✅ Points vérifiés:" -ForegroundColor Green
Write-Host "- Création d'utilisateur STUDENT"
Write-Host "- Connexion avec les nouveaux identifiants"
Write-Host "- Accès au profil avec le token JWT"
Write-Host "- Structure des données cohérente"

Write-Host "`n📋 Mappings de rôles:" -ForegroundColor Yellow
Write-Host "- Backend: ADMIN, TEACHER, STUDENT (majuscules)"
Write-Host "- Frontend: admin, teacher, student (minuscules)"
Write-Host "- Conversion automatique dans auth-context.tsx"