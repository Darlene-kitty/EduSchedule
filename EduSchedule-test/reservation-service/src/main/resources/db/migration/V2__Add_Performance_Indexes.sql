-- Indexes pour optimiser les performances des requêtes de réservation

-- Index composé pour la détection de conflits (le plus critique)
CREATE INDEX IF NOT EXISTS idx_reservations_conflict_detection 
ON reservations (resource_id, status, start_time, end_time);

-- Index pour les recherches par ressource et date
CREATE INDEX IF NOT EXISTS idx_reservations_resource_date 
ON reservations (resource_id, start_time, status);

-- Index pour les recherches par utilisateur
CREATE INDEX IF NOT EXISTS idx_reservations_user_date 
ON reservations (user_id, start_time DESC);

-- Index pour les recherches par cours
CREATE INDEX IF NOT EXISTS idx_reservations_course 
ON reservations (course_id, start_time);

-- Index pour les recherches par groupe de cours
CREATE INDEX IF NOT EXISTS idx_reservations_course_group 
ON reservations (course_group_id, start_time);

-- Index pour les réservations par statut
CREATE INDEX IF NOT EXISTS idx_reservations_status_created 
ON reservations (status, created_at);

-- Index pour les réservations par type
CREATE INDEX IF NOT EXISTS idx_reservations_type_date 
ON reservations (type, start_time);

-- Index pour les réservations récurrentes
CREATE INDEX IF NOT EXISTS idx_reservations_recurring 
ON reservations (parent_reservation_id, start_time) 
WHERE parent_reservation_id IS NOT NULL;

-- Index pour les statistiques d'occupation
CREATE INDEX IF NOT EXISTS idx_reservations_stats 
ON reservations (resource_id, start_time, end_time, status) 
WHERE status IN ('CONFIRMED', 'COMPLETED');

-- Index partiel pour les réservations actives (optimisation mémoire)
CREATE INDEX IF NOT EXISTS idx_reservations_active 
ON reservations (resource_id, start_time, end_time) 
WHERE status IN ('PENDING', 'CONFIRMED');

-- Index pour les recherches par plage de dates
CREATE INDEX IF NOT EXISTS idx_reservations_date_range 
ON reservations (start_time, end_time, status);

-- Index pour les requêtes de nettoyage et maintenance
CREATE INDEX IF NOT EXISTS idx_reservations_cleanup 
ON reservations (created_at, status) 
WHERE status IN ('CANCELLED', 'REJECTED', 'COMPLETED');

-- Statistiques pour l'optimiseur de requêtes
ANALYZE reservations;