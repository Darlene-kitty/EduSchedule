#!/bin/bash

# ============================================
# Script de Test de l'Authentification
# ============================================

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }

API_URL="http://localhost:8080"

echo "🔐 Test de l'Authentification IUSJC Planning"
echo "============================================"
echo ""

# Test 1: Inscription
print_info "Test 1: Inscription d'un nouvel utilisateur"
REGISTER_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@iusjc.cm",
    "password": "test123",
    "role": "STUDENT"
  }')

if echo "$REGISTER_RESPONSE" | grep -q "username"; then
    print_success "Inscription réussie"
    echo "$REGISTER_RESPONSE" | jq '.'
else
    print_error "Échec de l'inscription"
    echo "$REGISTER_RESPONSE"
fi

echo ""

# Test 2: Connexion
print_info "Test 2: Connexion avec les identifiants"
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    print_success "Connexion réussie"
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
    echo "Token: ${TOKEN:0:50}..."
else
    print_error "Échec de la connexion"
    echo "$LOGIN_RESPONSE"
    exit 1
fi

echo ""

# Test 3: Profil utilisateur
print_info "Test 3: Récupération du profil avec token"
ME_RESPONSE=$(curl -s -X GET "$API_URL/api/auth/me" \
  -H "Authorization: Bearer $TOKEN")

if echo "$ME_RESPONSE" | grep -q "username"; then
    print_success "Profil récupéré avec succès"
    echo "$ME_RESPONSE" | jq '.'
else
    print_error "Échec de récupération du profil"
    echo "$ME_RESPONSE"
fi

echo ""

# Test 4: Accès sans token (doit échouer)
print_info "Test 4: Accès sans token (doit échouer)"
NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/api/users")
HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    print_success "Accès refusé comme attendu (HTTP $HTTP_CODE)"
else
    print_error "Accès autorisé alors qu'il ne devrait pas (HTTP $HTTP_CODE)"
fi

echo ""

# Test 5: Connexion avec mauvais mot de passe (doit échouer)
print_info "Test 5: Connexion avec mauvais mot de passe (doit échouer)"
BAD_LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "wrongpassword"
  }')
HTTP_CODE=$(echo "$BAD_LOGIN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ]; then
    print_success "Connexion refusée comme attendu (HTTP $HTTP_CODE)"
else
    print_error "Connexion autorisée avec mauvais mot de passe (HTTP $HTTP_CODE)"
fi

echo ""

# Test 6: Inscription avec username existant (doit échouer)
print_info "Test 6: Inscription avec username existant (doit échouer)"
DUP_REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "another@iusjc.cm",
    "password": "test123",
    "role": "STUDENT"
  }')
HTTP_CODE=$(echo "$DUP_REGISTER_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "400" ]; then
    print_success "Inscription refusée comme attendu (HTTP $HTTP_CODE)"
else
    print_error "Inscription autorisée avec username existant (HTTP $HTTP_CODE)"
fi

echo ""
echo "============================================"
print_success "Tests d'authentification terminés !"
echo "============================================"
