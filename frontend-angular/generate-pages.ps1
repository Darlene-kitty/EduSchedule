# Script pour générer toutes les pages Angular

$pages = @(
    @{Name="login"; Title="Connexion"; Subtitle="Accédez à votre espace"},
    @{Name="register"; Title="Inscription"; Subtitle="Créez votre compte"},
    @{Name="welcome"; Title="Bienvenue"; Subtitle=""},
    @{Name="profile"; Title="Mon Profil"; Subtitle="Gérez vos informations personnelles"},
    @{Name="courses"; Title="Cours"; Subtitle="Gérez les cours et programmes"},
    @{Name="users"; Title="Utilisateurs"; Subtitle="Gestion des utilisateurs"},
    @{Name="schedule"; Title="Emplois du temps"; Subtitle="Planification des cours"},
    @{Name="calendar"; Title="Calendrier"; Subtitle="Vue calendrier des événements"},
    @{Name="reservations"; Title="Réservations"; Subtitle="Gestion des réservations de salles"},
    @{Name="resources"; Title="Ressources"; Subtitle="Gestion des ressources pédagogiques"},
    @{Name="rooms"; Title="Salles"; Subtitle="Gestion des salles de cours"},
    @{Name="conflicts"; Title="Conflits"; Subtitle="Résolution des conflits d'horaires"},
    @{Name="notifications"; Title="Notifications"; Subtitle="Centre de notifications"},
    @{Name="reports"; Title="Rapports"; Subtitle="Rapports et statistiques"},
    @{Name="teacher-availability"; Title="Disponibilités"; Subtitle="Gestion des disponibilités enseignants"},
    @{Name="events"; Title="Événements"; Subtitle="Gestion des événements académiques"}
)

foreach ($page in $pages) {
    $pageName = $page.Name
    $pageTitle = $page.Title
    $pageSubtitle = $page.Subtitle
    $className = (Get-Culture).TextInfo.ToTitleCase($pageName.Replace("-", " ")).Replace(" ", "")
    
    $folderPath = "src/app/features/$pageName"
    
    # Créer le dossier s'il n'existe pas
    if (!(Test-Path $folderPath)) {
        New-Item -ItemType Directory -Path $folderPath -Force | Out-Null
    }
    
    # Créer le fichier TypeScript
    $tsContent = @"
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppLayoutComponent } from '../../shared/components/app-layout/app-layout.component';

@Component({
  selector: 'app-$pageName',
  standalone: true,
  imports: [CommonModule, AppLayoutComponent],
  templateUrl: './$pageName.component.html',
  styleUrls: ['./$pageName.component.css']
})
export class ${className}Component {
  // Component logic here
}
"@
    
    # Créer le fichier HTML
    $htmlContent = @"
<app-layout activePage="$pageName" title="$pageTitle" subtitle="$pageSubtitle">
  <div class="container">
    <h2 class="text-2xl font-bold mb-4">$pageTitle</h2>
    <p class="text-gray-600">Page en cours de développement...</p>
  </div>
</app-layout>
"@
    
    # Créer le fichier CSS
    $cssContent = "/* Styles for $pageName component */"
    
    # Écrire les fichiers
    Set-Content -Path "$folderPath/$pageName.component.ts" -Value $tsContent
    Set-Content -Path "$folderPath/$pageName.component.html" -Value $htmlContent
    Set-Content -Path "$folderPath/$pageName.component.css" -Value $cssContent
    
    Write-Host "✓ Created $pageName component" -ForegroundColor Green
}

Write-Host "`n✅ All pages generated successfully!" -ForegroundColor Green
Write-Host "📝 Don't forget to update app.routes.ts with the new routes" -ForegroundColor Yellow
"@
