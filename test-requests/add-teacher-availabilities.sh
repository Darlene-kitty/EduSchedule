#!/bin/bash
##############################################################
#  add-teacher-availabilities.sh
#  Ajoute des disponibilités en masse pour plusieurs enseignants
#  via l'API /api/teacher-availability/bulk
#
#  Usage:
#    ./add-teacher-availabilities.sh
#    ./add-teacher-availabilities.sh http://localhost:8080 admin admin123
##############################################################

BASE_URL="${1:-http://localhost:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-admin123}"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

function write_ok()    { echo -e "${GREEN}  [OK]  $1${NC}"; }
function write_err()   { echo -e "${RED}  [ERR] $1${NC}"; }
function write_info()  { echo -e "${CYAN}  [..] $1${NC}"; }
function write_title() {
    echo ""
    echo -e "${YELLOW}══════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}  $1${NC}"
    echo -e "${YELLOW}══════════════════════════════════════════════${NC}"
}

# ─────────────────────────────────────────────
# Authentification
# ─────────────────────────────────────────────
write_title "AUTHENTIFICATION"
write_info "Login en tant que '$USERNAME' sur $BASE_URL ..."

LOGIN_BODY="{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}"
LOGIN_RESP=$(curl -s -w "\n__STATUS__%{http_code}" -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_BODY")

STATUS=$(echo "$LOGIN_RESP" | grep "__STATUS__" | cut -d'%' -f2)
BODY=$(echo "$LOGIN_RESP" | sed '/__STATUS__/d')

if [ "$STATUS" != "200" ]; then
    write_err "Login échoué (HTTP $STATUS)"
    echo "$BODY"
    exit 1
fi

TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    write_err "Token introuvable dans la réponse"
    echo "$BODY"
    exit 1
fi
write_ok "Authentifié — token: ${TOKEN:0:40}..."

# ─────────────────────────────────────────────
# Définition des disponibilités par enseignant
# ─────────────────────────────────────────────

# Fonction pour créer les disponibilités d'un enseignant
function add_availabilities() {
    local TEACHER_ID=$1
    local BULK_JSON=$2
    
    write_info "Enseignant ID=$TEACHER_ID — Envoi des créneaux..."
    
    RESP=$(curl -s -w "\n__STATUS__%{http_code}" -X POST \
        "$BASE_URL/api/teacher-availability/bulk" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "$BULK_JSON")
    
    STATUS=$(echo "$RESP" | grep "__STATUS__" | cut -d'%' -f2)
    BODY=$(echo "$RESP" | sed '/__STATUS__/d')
    
    if [ "$STATUS" = "201" ] || [ "$STATUS" = "200" ]; then
        COUNT=$(echo "$BODY" | grep -o '"id"' | wc -l)
        write_ok "Enseignant ID=$TEACHER_ID — $COUNT créneaux créés (HTTP $STATUS)"
        return 0
    else
        write_err "Enseignant ID=$TEACHER_ID — Échec (HTTP $STATUS)"
        echo "    $BODY"
        return 1
    fi
}

write_title "AJOUT DES DISPONIBILITÉS"

# ── Alain Mbarga (teacher1, id=2) ──────────────────────────
TEACHER_2='[
  {"teacherId":2,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi matin - cours magistraux"},
  {"teacherId":2,"dayOfWeek":"MONDAY","startTime":"14:00:00","endTime":"17:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":2,"notes":"Lundi après-midi - TD préféré"},
  {"teacherId":2,"dayOfWeek":"TUESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mardi matin"},
  {"teacherId":2,"dayOfWeek":"WEDNESDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"UNAVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mercredi - indisponible (recherche)"},
  {"teacherId":2,"dayOfWeek":"THURSDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Jeudi matin"},
  {"teacherId":2,"dayOfWeek":"FRIDAY","startTime":"08:00:00","endTime":"10:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Vendredi matin tôt"}
]'
add_availabilities 2 "$TEACHER_2"

# ── Pierre Essama (teacher2, id=3) ─────────────────────────
TEACHER_3='[
  {"teacherId":3,"dayOfWeek":"MONDAY","startTime":"10:00:00","endTime":"13:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi - disponible à partir de 10h"},
  {"teacherId":3,"dayOfWeek":"TUESDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":1,"notes":"Mardi - journée complète préférée"},
  {"teacherId":3,"dayOfWeek":"WEDNESDAY","startTime":"14:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":2,"notes":"Mercredi après-midi"},
  {"teacherId":3,"dayOfWeek":"THURSDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Jeudi matin"},
  {"teacherId":3,"dayOfWeek":"FRIDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"UNAVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Vendredi - indisponible"}
]'
add_availabilities 3 "$TEACHER_3"

# ── Samuel Nkoa (teacher3, id=4) ───────────────────────────
TEACHER_4='[
  {"teacherId":4,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi - toute la journée"},
  {"teacherId":4,"dayOfWeek":"TUESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mardi matin"},
  {"teacherId":4,"dayOfWeek":"WEDNESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":2,"notes":"Mercredi matin - préféré"},
  {"teacherId":4,"dayOfWeek":"THURSDAY","startTime":"14:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Jeudi après-midi"},
  {"teacherId":4,"dayOfWeek":"FRIDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Vendredi matin"}
]'
add_availabilities 4 "$TEACHER_4"

# ── Marie Ateba (teacher4, id=5) ───────────────────────────
TEACHER_5='[
  {"teacherId":5,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":1,"notes":"Lundi matin - préféré"},
  {"teacherId":5,"dayOfWeek":"TUESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mardi matin"},
  {"teacherId":5,"dayOfWeek":"TUESDAY","startTime":"14:00:00","endTime":"17:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":2,"notes":"Mardi après-midi"},
  {"teacherId":5,"dayOfWeek":"WEDNESDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"UNAVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mercredi - indisponible"},
  {"teacherId":5,"dayOfWeek":"THURSDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Jeudi - toute la journée"},
  {"teacherId":5,"dayOfWeek":"FRIDAY","startTime":"10:00:00","endTime":"14:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":2,"notes":"Vendredi milieu de journée"}
]'
add_availabilities 5 "$TEACHER_5"

# ── Robert Nganou (teacher5, id=6) ─────────────────────────
TEACHER_6='[
  {"teacherId":6,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi matin"},
  {"teacherId":6,"dayOfWeek":"MONDAY","startTime":"14:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi après-midi"},
  {"teacherId":6,"dayOfWeek":"TUESDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"UNAVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mardi - indisponible (autre établissement)"},
  {"teacherId":6,"dayOfWeek":"WEDNESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mercredi matin"},
  {"teacherId":6,"dayOfWeek":"THURSDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":1,"notes":"Jeudi matin - préféré"},
  {"teacherId":6,"dayOfWeek":"FRIDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Vendredi - toute la journée"}
]'
add_availabilities 6 "$TEACHER_6"

# ── Paul Essomba (teacher6, id=7) ──────────────────────────
TEACHER_7='[
  {"teacherId":7,"dayOfWeek":"MONDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Lundi matin"},
  {"teacherId":7,"dayOfWeek":"TUESDAY","startTime":"14:00:00","endTime":"18:00:00","availabilityType":"PREFERRED","isRecurring":true,"priorityLevel":2,"notes":"Mardi après-midi - préféré"},
  {"teacherId":7,"dayOfWeek":"WEDNESDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mercredi matin"},
  {"teacherId":7,"dayOfWeek":"WEDNESDAY","startTime":"14:00:00","endTime":"18:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Mercredi après-midi"},
  {"teacherId":7,"dayOfWeek":"THURSDAY","startTime":"08:00:00","endTime":"18:00:00","availabilityType":"UNAVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Jeudi - indisponible"},
  {"teacherId":7,"dayOfWeek":"FRIDAY","startTime":"08:00:00","endTime":"12:00:00","availabilityType":"AVAILABLE","isRecurring":true,"priorityLevel":1,"notes":"Vendredi matin"}
]'
add_availabilities 7 "$TEACHER_7"

# ─────────────────────────────────────────────
# Vérification : lire les disponibilités créées
# ─────────────────────────────────────────────
write_title "VÉRIFICATION"

for TID in 2 3 4 5 6 7; do
    RESP=$(curl -s -w "\n__STATUS__%{http_code}" -X GET \
        "$BASE_URL/api/teacher-availability/teacher/$TID" \
        -H "Authorization: Bearer $TOKEN")
    
    STATUS=$(echo "$RESP" | grep "__STATUS__" | cut -d'%' -f2)
    BODY=$(echo "$RESP" | sed '/__STATUS__/d')
    
    if [ "$STATUS" = "200" ]; then
        COUNT=$(echo "$BODY" | grep -o '"id"' | wc -l)
        write_ok "Enseignant ID=$TID — $COUNT disponibilité(s) en base (HTTP 200)"
    else
        write_err "Enseignant ID=$TID — Lecture échouée (HTTP $STATUS)"
    fi
done

write_title "TERMINÉ"
echo ""
