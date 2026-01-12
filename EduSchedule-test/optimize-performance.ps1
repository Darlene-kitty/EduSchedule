#!/usr/bin/env pwsh

Write-Host "=== OPTIMISATION DE PERFORMANCE - EduSchedule ===" -ForegroundColor Green
Write-Host "Application des optimisations de performance sur tous les services" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"

Write-Host "`n🚀 PHASE 1: OPTIMISATIONS BASE DE DONNÉES" -ForegroundColor Magenta

# Créer un script SQL d'optimisation
$optimizationSQL = @"
-- Optimisations de performance pour EduSchedule
USE iusjcdb;

-- Index pour améliorer les performances des requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_users_role_enabled ON users(role, enabled);
CREATE INDEX IF NOT EXISTS idx_users_email_enabled ON users(email, enabled);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Index pour les réservations
CREATE INDEX IF NOT EXISTS idx_reservations_status_date ON reservations(status, start_date_time);
CREATE INDEX IF NOT EXISTS idx_reservations_resource_date ON reservations(resource_id, start_date_time);
CREATE INDEX IF NOT EXISTS idx_reservations_user_status ON reservations(user_id, status);

-- Index pour les emplois du temps
CREATE INDEX IF NOT EXISTS idx_schedules_date_range ON schedules(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_schedules_room_date ON schedules(room, start_time);
CREATE INDEX IF NOT EXISTS idx_schedules_teacher_date ON schedules(teacher, start_time);

-- Index pour les cours
CREATE INDEX IF NOT EXISTS idx_courses_department_level ON courses(department, level);
CREATE INDEX IF NOT EXISTS idx_courses_teacher_active ON courses(teacher_id, active);

-- Index pour les notifications
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status ON notifications(recipient, status);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- Index pour les rapports
CREATE INDEX IF NOT EXISTS idx_reports_user_status ON reports(generated_by, status);
CREATE INDEX IF NOT EXISTS idx_reports_type_created ON reports(type, created_at);

-- Optimisation des tables
OPTIMIZE TABLE users;
OPTIMIZE TABLE reservations;
OPTIMIZE TABLE schedules;
OPTIMIZE TABLE courses;
OPTIMIZE TABLE notifications;
OPTIMIZE TABLE reports;

SELECT 'Optimisations de base de données appliquées' as status;
"@

Write-Host "Création du script d'optimisation SQL..." -ForegroundColor Cyan
$optimizationSQL | Out-File -FilePath "database-optimization.sql" -Encoding UTF8

Write-Host "✅ Script d'optimisation créé: database-optimization.sql" -ForegroundColor Green
Write-Host "Pour l'appliquer: mysql -u root -p < database-optimization.sql" -ForegroundColor Yellow

Write-Host "`n⚙️ PHASE 2: OPTIMISATIONS CONFIGURATION SERVICES" -ForegroundColor Magenta

# Optimisations pour application.properties
$performanceOptimizations = @{
    "user-service" = @"
# Optimisations de performance ajoutées
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Pool de connexions optimisé
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000

# Cache de second niveau
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory

# Optimisations JVM
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.max-connections=8192
server.tomcat.accept-count=100
"@

    "course-service" = @"
# Optimisations de performance
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=3
server.tomcat.threads.max=150
"@

    "reservation-service" = @"
# Optimisations de performance
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=3
server.tomcat.threads.max=150

# Cache pour les conflits de réservation
spring.cache.type=simple
"@

    "scheduling-service" = @"
# Optimisations de performance
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=3
server.tomcat.threads.max=150

# Configuration Redis pour cache
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
spring.redis.jedis.pool.min-idle=0
"@

    "reporting-service" = @"
# Optimisations de performance
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
server.tomcat.threads.max=100

# Configuration pour les gros rapports
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
server.tomcat.max-swallow-size=50MB
"@
}

foreach ($service in $performanceOptimizations.Keys) {
    Write-Host "`nOptimisation de $service..." -ForegroundColor Cyan
    $configFile = "$service/src/main/resources/application-performance.properties"
    
    if (Test-Path "$service/src/main/resources/") {
        $performanceOptimizations[$service] | Out-File -FilePath $configFile -Encoding UTF8
        Write-Host "✅ Configuration de performance créée: $configFile" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Service $service non trouvé, ignoré" -ForegroundColor Yellow
    }
}

Write-Host "`n🔧 PHASE 3: OPTIMISATIONS JVM" -ForegroundColor Magenta

# Créer un script de démarrage optimisé
$jvmOptimizations = @"
#!/usr/bin/env pwsh

# Script de démarrage optimisé pour EduSchedule
# Utilise des paramètres JVM optimisés pour la performance

`$JVM_OPTS = @(
    "-Xms512m",
    "-Xmx2g",
    "-XX:+UseG1GC",
    "-XX:G1HeapRegionSize=16m",
    "-XX:+UseStringDeduplication",
    "-XX:+OptimizeStringConcat",
    "-XX:+UseCompressedOops",
    "-XX:+UseCompressedClassPointers",
    "-Djava.awt.headless=true",
    "-Dspring.profiles.active=performance"
)

Write-Host "=== Démarrage optimisé des services EduSchedule ===" -ForegroundColor Green
Write-Host "Paramètres JVM: `$(`$JVM_OPTS -join ' ')" -ForegroundColor Cyan

# Fonction pour démarrer un service avec optimisations
function Start-OptimizedService {
    param([string]`$ServiceName, [int]`$Port)
    
    Write-Host "`nDémarrage de `$ServiceName (port `$Port)..." -ForegroundColor Yellow
    
    Set-Location `$ServiceName
    try {
        `$env:MAVEN_OPTS = `$JVM_OPTS -join ' '
        Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Normal
        Write-Host "✅ `$ServiceName démarré avec optimisations" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur démarrage `$ServiceName" -ForegroundColor Red
    } finally {
        Set-Location ..
    }
    
    Start-Sleep -Seconds 5
}

# Démarrage séquentiel optimisé
Start-OptimizedService "eureka-server" 8761
Start-Sleep -Seconds 30

Start-OptimizedService "user-service" 8081
Start-OptimizedService "resource-service" 8083
Start-OptimizedService "course-service" 8084
Start-OptimizedService "reservation-service" 8085
Start-OptimizedService "scheduling-service" 8086
Start-OptimizedService "notification-service" 8082
Start-OptimizedService "reporting-service" 8088

Start-Sleep -Seconds 20
Start-OptimizedService "api-gateway" 8080

Write-Host "`n✅ Tous les services démarrés avec optimisations de performance!" -ForegroundColor Green
"@

$jvmOptimizations | Out-File -FilePath "start-optimized.ps1" -Encoding UTF8
Write-Host "✅ Script de démarrage optimisé créé: start-optimized.ps1" -ForegroundColor Green

Write-Host "`n📊 PHASE 4: CONFIGURATION MONITORING" -ForegroundColor Magenta

# Configuration de monitoring
$monitoringConfig = @"
# Configuration de monitoring pour tous les services
# À ajouter dans application.properties

# Actuator endpoints pour monitoring
management.endpoints.web.exposure.include=health,info,metrics,prometheus,env
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true
management.metrics.export.prometheus.enabled=true

# Métriques personnalisées
management.metrics.enable.jvm=true
management.metrics.enable.system=true
management.metrics.enable.web=true
management.metrics.enable.tomcat=true

# Logging optimisé
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Cache configuration
spring.cache.cache-names=users,courses,reservations,statistics
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=300s
"@

$monitoringConfig | Out-File -FilePath "monitoring-config.properties" -Encoding UTF8
Write-Host "✅ Configuration de monitoring créée: monitoring-config.properties" -ForegroundColor Green

Write-Host "`n🔍 PHASE 5: SCRIPT DE VÉRIFICATION PERFORMANCE" -ForegroundColor Magenta

$performanceCheck = @"
#!/usr/bin/env pwsh

Write-Host "=== VÉRIFICATION DE PERFORMANCE ===" -ForegroundColor Green

# Test de charge simple
`$services = @{
    "User Service" = "http://localhost:8081/actuator/health"
    "Course Service" = "http://localhost:8084/actuator/health"
    "Reservation Service" = "http://localhost:8085/actuator/health"
    "Reporting Service" = "http://localhost:8088/api/v1/reports/test"
}

foreach (`$service in `$services.Keys) {
    Write-Host "`nTest de performance `$service..." -ForegroundColor Cyan
    
    `$times = @()
    for (`$i = 1; `$i -le 10; `$i++) {
        `$start = Get-Date
        try {
            Invoke-WebRequest -Uri `$services[`$service] -Method GET -TimeoutSec 5 | Out-Null
            `$end = Get-Date
            `$duration = (`$end - `$start).TotalMilliseconds
            `$times += `$duration
        } catch {
            Write-Host "❌ Erreur requête `$i" -ForegroundColor Red
        }
    }
    
    if (`$times.Count -gt 0) {
        `$avgTime = (`$times | Measure-Object -Average).Average
        `$minTime = (`$times | Measure-Object -Minimum).Minimum
        `$maxTime = (`$times | Measure-Object -Maximum).Maximum
        
        Write-Host "✅ `$service - Temps moyen: `$([math]::Round(`$avgTime, 2))ms" -ForegroundColor Green
        Write-Host "   Min: `$([math]::Round(`$minTime, 2))ms, Max: `$([math]::Round(`$maxTime, 2))ms" -ForegroundColor Yellow
        
        if (`$avgTime -lt 100) {
            Write-Host "   🚀 Performance excellente!" -ForegroundColor Green
        } elseif (`$avgTime -lt 500) {
            Write-Host "   ✅ Performance correcte" -ForegroundColor Yellow
        } else {
            Write-Host "   ⚠️ Performance à améliorer" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== Vérification terminée ===" -ForegroundColor Green
"@

$performanceCheck | Out-File -FilePath "check-performance.ps1" -Encoding UTF8
Write-Host "✅ Script de vérification performance créé: check-performance.ps1" -ForegroundColor Green

Write-Host "`n📋 RÉSUMÉ DES OPTIMISATIONS APPLIQUÉES" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor White

Write-Host "✅ Base de données:" -ForegroundColor Green
Write-Host "   - Index optimisés pour requêtes fréquentes" -ForegroundColor Gray
Write-Host "   - Tables optimisées" -ForegroundColor Gray

Write-Host "✅ Configuration services:" -ForegroundColor Green
Write-Host "   - Pool de connexions optimisé" -ForegroundColor Gray
Write-Host "   - Batch processing activé" -ForegroundColor Gray
Write-Host "   - Cache de second niveau" -ForegroundColor Gray

Write-Host "✅ JVM:" -ForegroundColor Green
Write-Host "   - Garbage Collector G1" -ForegroundColor Gray
Write-Host "   - Optimisations mémoire" -ForegroundColor Gray
Write-Host "   - Profil performance activé" -ForegroundColor Gray

Write-Host "✅ Monitoring:" -ForegroundColor Green
Write-Host "   - Métriques Prometheus" -ForegroundColor Gray
Write-Host "   - Endpoints de santé détaillés" -ForegroundColor Gray
Write-Host "   - Logging optimisé" -ForegroundColor Gray

Write-Host "`n🚀 PROCHAINES ÉTAPES:" -ForegroundColor Yellow
Write-Host "1. Appliquer les optimisations DB: mysql -u root -p < database-optimization.sql" -ForegroundColor Cyan
Write-Host "2. Redémarrer avec optimisations: .\start-optimized.ps1" -ForegroundColor Cyan
Write-Host "3. Vérifier les performances: .\check-performance.ps1" -ForegroundColor Cyan
Write-Host "4. Surveiller avec: http://localhost:8080/actuator/metrics" -ForegroundColor Cyan

Write-Host "`n=== OPTIMISATION DE PERFORMANCE TERMINÉE ===" -ForegroundColor Green
"@

$optimizeScript | Out-File -FilePath "optimize-performance.ps1" -Encoding UTF8