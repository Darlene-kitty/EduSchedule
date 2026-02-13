# Script pour aider à copier le contenu React vers Angular

Write-Host "🔄 Guide de copie du contenu React vers Angular" -ForegroundColor Cyan
Write-Host ""

$pages = @(
    @{React="courses-view.tsx"; Angular="courses"; Description="Gestion des cours"},
    @{React="users-view.tsx"; Angular="users"; Description="Gestion des utilisateurs"},
    @{React="schedule-view.tsx"; Angular="schedule"; Description="Emplois du temps"},
    @{React="calendar-view.tsx"; Angular="calendar"; Description="Calendrier"},
    @{React="reservations-view.tsx"; Angular="reservations"; Description="Réservations"},
    @{React="resources-view.tsx"; Angular="resources"; Description="Ressources"},
    @{React="rooms-view.tsx"; Angular="rooms"; Description="Salles"},
    @{React="conflicts-view.tsx"; Angular="conflicts"; Description="Conflits"},
    @{React="notifications-view.tsx"; Angular="notifications"; Description="Notifications"},
    @{React="reports-view.tsx"; Angular="reports"; Description="Rapports"},
    @{React="teacher-availability-view.tsx"; Angular="teacher-availability"; Description="Disponibilités"},
    @{React="events-view.tsx"; Angular="events"; Description="Événements"},
    @{React="profile-view.tsx"; Angular="profile"; Description="Profil utilisateur"}
)

Write-Host "📋 Pages à implémenter:" -ForegroundColor Yellow
Write-Host ""

foreach ($page in $pages) {
    $reactFile = "../frontend/components/$($page.React)"
    $angularFolder = "src/app/features/$($page.Angular)"
    
    if (Test-Path $reactFile) {
        Write-Host "✓ $($page.Description)" -ForegroundColor Green
        Write-Host "  React:   $reactFile" -ForegroundColor Gray
        Write-Host "  Angular: $angularFolder" -ForegroundColor Gray
        Write-Host ""
    } else {
        Write-Host "✗ $($page.Description) - Fichier React non trouvé" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Host ""
Write-Host "📝 Instructions pour chaque page:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Ouvrir le fichier React correspondant"
Write-Host "2. Copier la structure HTML du return/template"
Write-Host "3. Adapter la syntaxe:"
Write-Host "   - className → class"
Write-Host "   - onClick → (click)"
Write-Host "   - {variable} → {{variable}}"
Write-Host "   - {condition && <div>} → <div *ngIf='condition'>"
Write-Host "   - {array.map()} → <div *ngFor='let item of array'>"
Write-Host "4. Copier la logique TypeScript"
Write-Host "5. Adapter les hooks:"
Write-Host "   - useState → propriétés de classe"
Write-Host "   - useEffect → ngOnInit()"
Write-Host "6. Utiliser <app-layout> pour le wrapper"
Write-Host ""
Write-Host "✨ Exemple de template Angular:" -ForegroundColor Yellow
Write-Host @"
<app-layout activePage="courses" title="Cours" subtitle="Gérez les cours">
  <div header-actions>
    <button class="btn">Action</button>
  </div>
  
  <!-- Contenu de la page -->
  <div class="grid grid-cols-3 gap-4">
    <div *ngFor="let item of items" class="card">
      {{ item.name }}
    </div>
  </div>
</app-layout>
"@
Write-Host ""
Write-Host "🚀 Bon courage!" -ForegroundColor Green
