#!/usr/bin/env node

/**
 * Script de vérification de la configuration du frontend
 * Vérifie que tous les fichiers nécessaires sont présents
 */

const fs = require('fs');
const path = require('path');

const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
};

function log(message, color = colors.reset) {
  console.log(`${color}${message}${colors.reset}`);
}

function checkFile(filePath, description) {
  const exists = fs.existsSync(path.join(__dirname, filePath));
  if (exists) {
    log(`✓ ${description}`, colors.green);
    return true;
  } else {
    log(`✗ ${description} - MANQUANT`, colors.red);
    return false;
  }
}

function checkDirectory(dirPath, description) {
  const exists = fs.existsSync(path.join(__dirname, dirPath));
  if (exists) {
    log(`✓ ${description}`, colors.green);
    return true;
  } else {
    log(`✗ ${description} - MANQUANT`, colors.red);
    return false;
  }
}

console.log('\n' + '='.repeat(60));
log('🔍 Vérification de la Configuration Frontend', colors.blue);
console.log('='.repeat(60) + '\n');

let allGood = true;

// Vérifier les fichiers de configuration
log('\n📋 Fichiers de Configuration:', colors.yellow);
allGood &= checkFile('.env.local', '.env.local (configuration API)');
allGood &= checkFile('package.json', 'package.json');
allGood &= checkFile('tsconfig.json', 'tsconfig.json');
allGood &= checkFile('next.config.mjs', 'next.config.mjs');

// Vérifier la structure lib/
log('\n📚 Bibliothèques (lib/):', colors.yellow);
allGood &= checkFile('lib/api-config.ts', 'Configuration API');
allGood &= checkFile('lib/api-client.ts', 'Client HTTP');
allGood &= checkFile('lib/utils.ts', 'Utilitaires');
allGood &= checkFile('lib/session.ts', 'Gestion de session');
allGood &= checkFile('lib/permissions.ts', 'Gestion des permissions');

// Vérifier les services
log('\n🔌 Services API (services/):', colors.yellow);
allGood &= checkFile('services/auth.service.ts', 'Service d\'authentification');
allGood &= checkFile('services/user.service.ts', 'Service utilisateurs');
allGood &= checkFile('services/resource.service.ts', 'Service ressources');
allGood &= checkFile('services/course.service.ts', 'Service cours');
allGood &= checkFile('services/reservation.service.ts', 'Service réservations');
allGood &= checkFile('services/schedule.service.ts', 'Service emplois du temps');
allGood &= checkFile('services/notification.service.ts', 'Service notifications');
allGood &= checkFile('services/report.service.ts', 'Service rapports');
allGood &= checkFile('services/index.ts', 'Export des services');

// Vérifier les hooks
log('\n🪝 Hooks Personnalisés (hooks/):', colors.yellow);
allGood &= checkFile('hooks/use-api.ts', 'Hook useApi');
allGood &= checkFile('hooks/use-toast.ts', 'Hook useToast');
allGood &= checkFile('hooks/use-mobile.ts', 'Hook useMobile');

// Vérifier les contextes
log('\n🌐 Contextes React (contexts/):', colors.yellow);
allGood &= checkFile('contexts/auth-context.tsx', 'Contexte d\'authentification');
allGood &= checkFile('contexts/toast-context.tsx', 'Contexte des toasts');

// Vérifier les composants principaux
log('\n🎨 Composants Principaux (components/):', colors.yellow);
allGood &= checkFile('components/login-view.tsx', 'Vue de connexion');
allGood &= checkFile('components/register-view.tsx', 'Vue d\'inscription');
allGood &= checkFile('components/users-view.tsx', 'Vue des utilisateurs');
allGood &= checkFile('components/resources-view.tsx', 'Vue des ressources');
allGood &= checkFile('components/auth-guard.tsx', 'Protection des routes');
allGood &= checkFile('components/header.tsx', 'En-tête');
allGood &= checkFile('components/sidebar.tsx', 'Barre latérale');

// Vérifier les exemples
log('\n📖 Exemples et Documentation:', colors.yellow);
allGood &= checkFile('components/users-view-example.tsx', 'Exemple de migration');
allGood &= checkFile('API_INTEGRATION.md', 'Documentation API');
allGood &= checkFile('MIGRATION_GUIDE.md', 'Guide de migration');
allGood &= checkFile('README.md', 'README');
allGood &= checkFile('INTEGRATION_SUMMARY.md', 'Résumé d\'intégration');

// Vérifier les dossiers
log('\n📁 Structure des Dossiers:', colors.yellow);
allGood &= checkDirectory('app', 'Dossier app/ (pages)');
allGood &= checkDirectory('components', 'Dossier components/');
allGood &= checkDirectory('components/ui', 'Dossier components/ui/');
allGood &= checkDirectory('lib', 'Dossier lib/');
allGood &= checkDirectory('services', 'Dossier services/');
allGood &= checkDirectory('hooks', 'Dossier hooks/');
allGood &= checkDirectory('contexts', 'Dossier contexts/');

// Vérifier node_modules
log('\n📦 Dépendances:', colors.yellow);
const nodeModulesExists = fs.existsSync(path.join(__dirname, 'node_modules'));
if (nodeModulesExists) {
  log('✓ node_modules/ installé', colors.green);
} else {
  log('✗ node_modules/ manquant - Exécutez: npm install', colors.red);
  allGood = false;
}

// Vérifier .env.local
log('\n⚙️  Configuration API:', colors.yellow);
const envExists = fs.existsSync(path.join(__dirname, '.env.local'));
if (envExists) {
  const envContent = fs.readFileSync(path.join(__dirname, '.env.local'), 'utf8');
  if (envContent.includes('NEXT_PUBLIC_API_BASE_URL')) {
    log('✓ NEXT_PUBLIC_API_BASE_URL configuré', colors.green);
  } else {
    log('✗ NEXT_PUBLIC_API_BASE_URL manquant dans .env.local', colors.red);
    allGood = false;
  }
} else {
  log('⚠ .env.local manquant - Créez-le avec:', colors.yellow);
  log('  NEXT_PUBLIC_API_BASE_URL=http://localhost:8080', colors.yellow);
  log('  NEXT_PUBLIC_API_TIMEOUT=30000', colors.yellow);
}

// Résumé
console.log('\n' + '='.repeat(60));
if (allGood) {
  log('✅ Configuration complète ! Vous pouvez démarrer le frontend.', colors.green);
  log('\nCommandes disponibles:', colors.blue);
  log('  npm run dev    - Démarrer en mode développement', colors.reset);
  log('  npm run build  - Build de production', colors.reset);
  log('  npm start      - Démarrer en production', colors.reset);
} else {
  log('❌ Configuration incomplète. Veuillez corriger les erreurs ci-dessus.', colors.red);
  process.exit(1);
}
console.log('='.repeat(60) + '\n');
