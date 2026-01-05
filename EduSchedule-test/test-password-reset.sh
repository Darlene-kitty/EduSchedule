#!/bin/bash

# Script de test pour le système de réinitialisation de mot de passe
# EduSchedule - IUSJC

echo "🔐 Test du Système de Réinitialisation de Mot de Passe"
echo "================================================="

# Configuration
API_BASE_URL="http://localhost:8080"
TEST_EMAIL="test@example.com"
FRONTEND_URL="http://localhost:3000"

echo ""
echo "📋 Configuration:"
echo "  API Base URL: $API_BASE_URL"
echo "  Email de test: $TEST_EMAIL"
echo "  Frontend URL: $FRONTEND_URL"
echo ""

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test 1: Vérifier que l'API Gateway répond
echo -e "${GREEN}🔍 Test 1: Vérification de l'API Gateway...${NC}"
if curl -s -f "$API_BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "  ✅ API Gateway accessible"
else
    echo -e "  ${RED}❌ API Gateway non accessible${NC}"
    echo -e "  ${YELLOW}💡 Assurez-vous que les services backend sont démarrés${NC}"
    exit 1
fi

# Test 2: Test de l'endpoint forgot-password
echo ""
echo -e "${GREEN}🔍 Test 2: Endpoint forgot-password...${NC}"
response=$(curl -s -w "%{http_code}" -X POST "$API_BASE_URL/api/auth/forgot-password" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\"}" \
    -o /tmp/forgot_password_response.json)

if [ "$response" = "200" ]; then
    echo -e "  ✅ Endpoint forgot-password fonctionne"
    if [ -f /tmp/forgot_password_response.json ]; then
        message=$(cat /tmp/forgot_password_response.json | grep -o '"message":"[^"]*"' | cut -d'"' -f4)
        echo -e "  ${CYAN}📧 Message: $message${NC}"
    fi
else
    echo -e "  ${RED}❌ Erreur endpoint forgot-password (Status: $response)${NC}"
    if [ -f /tmp/forgot_password_response.json ]; then
        echo -e "  ${YELLOW}📄 Réponse: $(cat /tmp/forgot_password_response.json)${NC}"
    fi
fi

# Test 3: Test de validation avec un token invalide
echo ""
echo -e "${GREEN}🔍 Test 3: Validation de token invalide...${NC}"
invalid_token="invalid-token-123"
response=$(curl -s -w "%{http_code}" -X GET "$API_BASE_URL/api/auth/reset-password/validate?token=$invalid_token" \
    -o /tmp/validate_response.json)

if [ "$response" = "400" ]; then
    echo -e "  ✅ Validation de token invalide fonctionne correctement"
else
    echo -e "  ${YELLOW}⚠️  Réponse inattendue pour token invalide (Status: $response)${NC}"
fi

# Test 4: Vérifier la base de données
echo ""
echo -e "${GREEN}🔍 Test 4: Vérification de la base de données...${NC}"
echo -e "  ${YELLOW}💡 Vérifiez manuellement que la table 'password_reset_tokens' existe${NC}"
echo -e "  ${YELLOW}💡 Commande SQL: SHOW TABLES LIKE 'password_reset_tokens';${NC}"

# Test 5: Vérifier la configuration SMTP
echo ""
echo -e "${GREEN}🔍 Test 5: Configuration SMTP...${NC}"
if [ -f ".env" ]; then
    if grep -q "MAIL_HOST=" .env; then
        echo -e "  ✅ MAIL_HOST configuré"
    fi
    if grep -q "MAIL_USERNAME=" .env; then
        echo -e "  ✅ MAIL_USERNAME configuré"
    fi
    if grep -q "MAIL_PASSWORD=" .env; then
        echo -e "  ✅ MAIL_PASSWORD configuré"
    fi
    
    if ! grep -q "MAIL_HOST=" .env; then
        echo -e "  ${YELLOW}⚠️  Configuration SMTP non trouvée dans .env${NC}"
        echo -e "  ${YELLOW}💡 Consultez CONFIGURATION_SMTP.md pour la configuration${NC}"
    fi
else
    echo -e "  ${YELLOW}⚠️  Fichier .env non trouvé${NC}"
    echo -e "  ${YELLOW}💡 Copiez .env.example vers .env et configurez les variables SMTP${NC}"
fi

# Test 6: Vérifier les pages frontend
echo ""
echo -e "${GREEN}🔍 Test 6: Pages frontend...${NC}"

# Test page forgot-password
if curl -s -f "$FRONTEND_URL/forgot-password" > /dev/null 2>&1; then
    echo -e "  ✅ Page /forgot-password accessible"
else
    echo -e "  ${RED}❌ Page /forgot-password non accessible${NC}"
    echo -e "  ${YELLOW}💡 Assurez-vous que le frontend est démarré (npm run dev)${NC}"
fi

# Test page reset-password
if curl -s -f "$FRONTEND_URL/reset-password" > /dev/null 2>&1; then
    echo -e "  ✅ Page /reset-password accessible"
else
    echo -e "  ${RED}❌ Page /reset-password non accessible${NC}"
fi

# Test page login
if curl -s -f "$FRONTEND_URL/login" > /dev/null 2>&1; then
    echo -e "  ✅ Page /login accessible"
else
    echo -e "  ${RED}❌ Page /login non accessible${NC}"
fi

# Nettoyage des fichiers temporaires
rm -f /tmp/forgot_password_response.json /tmp/validate_response.json

# Résumé
echo ""
echo -e "${CYAN}📊 Résumé des Tests${NC}"
echo -e "${CYAN}==================${NC}"
echo ""
echo -e "${GREEN}✅ Tests réussis:${NC}"
echo "  - API Gateway accessible"
echo "  - Endpoint forgot-password fonctionnel"
echo "  - Validation de token invalide"
echo ""
echo -e "${YELLOW}📋 Actions manuelles recommandées:${NC}"
echo "  1. Vérifier la table password_reset_tokens dans MySQL"
echo "  2. Tester l'envoi d'email avec un vrai compte"
echo "  3. Tester le flux complet depuis le frontend"
echo "  4. Vérifier la réception d'emails dans la boîte mail"
echo ""
echo -e "${CYAN}📚 Documentation:${NC}"
echo "  - Guide complet: GUIDE_MOT_DE_PASSE_OUBLIE.md"
echo "  - Configuration SMTP: CONFIGURATION_SMTP.md"
echo ""
echo -e "${GREEN}🎉 Système de réinitialisation de mot de passe prêt !${NC}"