# EduSchedule - Déploiement Docker

Guide rapide pour déployer EduSchedule avec Docker.

## 🚀 Démarrage Rapide

### Option 1 : Déploiement Automatique (Recommandé)

```powershell
# Déploiement complet en une commande
.\deploy-complete.ps1
```

### Option 2 : Démarrage Rapide

```cmd
# Si les images sont déjà construites
quick-start-docker.bat
```

### Option 3 : Étape par Étape

```cmd
# 1. Construire les images
build-docker-images.bat

# 2. Démarrer les services
docker-compose up -d

# 3. Vérifier le déploiement
powershell .\test-docker-deployment.ps1
```

## 📊 Monitoring

### Monitoring en Temps Réel

```powershell
# Affiche l'état de tous les services (rafraîchissement automatique)
.\monitor-services.ps1

# Avec intervalle personnalisé (en secondes)
.\monitor-services.ps1 -RefreshInterval 10
```

### Vérification de la Communication

```powershell
# Teste la communication entre services
.\verify-services-communication.ps1
```

## 🔧 Scripts Disponibles

| Script | Description | Usage |
|--------|-------------|-------|
| `deploy-complete.ps1` | Déploiement complet automatisé | `.\deploy-complete.ps1 [-SkipBuild] [-SkipTests] [-CleanStart]` |
| `quick-start-docker.bat` | Démarrage rapide | `quick-start-docker.bat` |
| `build-docker-images.bat` | Construction des images | `build-docker-images.bat` |
| `test-docker-deployment.ps1` | Test du déploiement | `.\test-docker-deployment.ps1` |
| `monitor-services.ps1` | Monitoring en temps réel | `.\monitor-services.ps1` |
| `verify-services-communication.ps1` | Vérification inter-services | `.\verify-services-communication.ps1` |

## 📦 Services Déployés

### Infrastructure (5 services)
- MySQL 8.0 (port 3306)
- Redis 7 (port 6379)
- RabbitMQ 3 (ports 5672, 15672)
- Eureka Server (port 8761)
- Zipkin (port 9411)

### Core (2 services)
- Config Server (port 8888)
- API Gateway (port 8080)

### Microservices (15 services)
- User Service (8081)
- Resource Service (8082)
- School Service (8083)
- Course Service (8084)
- Scheduling Service (8085)
- Reservation Service (8086)
- Notification Service (8087)
- Reporting Service (8088)
- Calendar Service (8089)
- Room Service (8090)
- Event Service (8091)
- Teacher Availability Service (8092)
- AI Service (8093)
- Maintenance Service (8094)
- ENT Integration Service (8095)

### Frontend (1 service)
- Next.js Application (port 3000)

**Total : 23 services**

## 🌐 URLs d'Accès

Une fois déployé :

- **Application** : http://localhost:3000
- **API Gateway** : http://localhost:8080
- **Eureka Dashboard** : http://localhost:8761
- **RabbitMQ Management** : http://localhost:15672 (iusjc/iusjc2025)
- **Zipkin Tracing** : http://localhost:9411

## ⏱️ Temps de Démarrage

- **Infrastructure** : ~30 secondes
- **Eureka Server** : ~40 secondes
- **Microservices** : ~2-3 minutes
- **Frontend** : ~20 secondes
- **Total** : ~4-5 minutes

## 🔍 Commandes Docker Utiles

### Gestion des Services

```powershell
# Voir l'état des conteneurs
docker-compose ps

# Voir les logs de tous les services
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f user-service

# Redémarrer un service
docker-compose restart user-service

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

### Debugging

```powershell
# Accéder au shell d'un conteneur
docker exec -it eduschedule-user-service sh

# Voir les variables d'environnement
docker exec eduschedule-user-service env

# Voir les ressources utilisées
docker stats

# Inspecter un conteneur
docker inspect eduschedule-user-service
```

### Nettoyage

```powershell
# Supprimer les conteneurs arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Nettoyage complet (ATTENTION)
docker system prune -a --volumes
```

## 🐛 Résolution des Problèmes

### Service ne démarre pas

```powershell
# 1. Voir les logs
docker-compose logs service-name

# 2. Vérifier l'état
docker-compose ps service-name

# 3. Redémarrer
docker-compose restart service-name
```

### Erreur de connexion MySQL

```powershell
# Vérifier MySQL
docker exec eduschedule-mysql mysqladmin ping -h localhost -uiusjc -piusjc2025

# Voir les logs
docker-compose logs mysql

# Recréer le conteneur
docker-compose down mysql
docker-compose up -d mysql
```

### Port déjà utilisé

```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Arrêter le processus ou changer le port dans docker-compose.yml
```

### Manque de mémoire

1. Augmenter la mémoire Docker Desktop (Settings > Resources > Memory : min 8 GB)
2. Ou démarrer seulement les services essentiels :

```powershell
docker-compose up -d mysql redis rabbitmq eureka-server api-gateway user-service frontend
```

## 📝 Configuration

### Variables d'Environnement

Modifier le fichier `.env` pour personnaliser :

```env
# Base de données
MYSQL_PASSWORD=votre_mot_de_passe

# JWT
JWT_SECRET=votre_cle_secrete

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=votre_email
MAIL_PASSWORD=votre_mot_de_passe
```

### Limites de Ressources

Ajouter dans `docker-compose.yml` :

```yaml
services:
  user-service:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
```

## 🔒 Sécurité

Pour la production :

1. ✅ Changer tous les mots de passe par défaut
2. ✅ Générer une nouvelle clé JWT secrète
3. ✅ Configurer HTTPS avec certificats SSL
4. ✅ Limiter l'exposition des ports
5. ✅ Activer les pare-feu
6. ✅ Mettre en place des backups automatiques

## 📚 Documentation Complète

Pour plus de détails, consultez :
- [GUIDE_DEPLOIEMENT_DOCKER.md](GUIDE_DEPLOIEMENT_DOCKER.md) - Guide complet
- [README.md](README.md) - Documentation générale
- [AUDIT_SECURITE.md](AUDIT_SECURITE.md) - Audit de sécurité

## 💾 Backup

```powershell
# Backup de la base de données
docker exec eduschedule-mysql mysqldump -uiusjc -piusjc2025 iusjcdb > backup.sql

# Restaurer
docker exec -i eduschedule-mysql mysql -uiusjc -piusjc2025 iusjcdb < backup.sql
```

## 🆘 Support

En cas de problème :

1. Vérifier les logs : `docker-compose logs -f`
2. Exécuter le monitoring : `.\monitor-services.ps1`
3. Vérifier la communication : `.\verify-services-communication.ps1`
4. Consulter [GUIDE_DEPLOIEMENT_DOCKER.md](GUIDE_DEPLOIEMENT_DOCKER.md)

---

**Note** : Assurez-vous d'avoir au moins 8 GB de RAM et 20 GB d'espace disque disponibles.
