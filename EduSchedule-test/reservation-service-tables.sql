-- =====================================================
-- Script d'initialisation des tables - Reservation Service
-- =====================================================

-- Création de la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS reservation_service_db 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE reservation_service_db;

-- =====================================================
-- Table des réservations
-- =====================================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Références aux autres services
    resource_id BIGINT NOT NULL COMMENT 'ID de la ressource/salle (resource-service)',
    course_id BIGINT NULL COMMENT 'ID du cours (course-service)',
    course_group_id BIGINT NULL COMMENT 'ID du groupe de cours (course-service)',
    user_id BIGINT NOT NULL COMMENT 'ID de l\'utilisateur (user-service)',
    
    -- Informations de base
    title VARCHAR(100) NOT NULL COMMENT 'Titre de la réservation',
    description VARCHAR(500) NULL COMMENT 'Description détaillée',
    
    -- Horaires
    start_time DATETIME NOT NULL COMMENT 'Heure de début',
    end_time DATETIME NOT NULL COMMENT 'Heure de fin',
    
    -- Statut et type
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED') 
        NOT NULL DEFAULT 'PENDING' COMMENT 'Statut de la réservation',
    type ENUM('COURSE', 'EXAM', 'MEETING', 'EVENT', 'MAINTENANCE', 'OTHER') 
        NOT NULL COMMENT 'Type de réservation',
    
    -- Récurrence
    recurring_pattern TEXT NULL COMMENT 'Pattern de récurrence (JSON)',
    parent_reservation_id BIGINT NULL COMMENT 'ID de la réservation parent pour les récurrences',
    
    -- Détails logistiques
    expected_attendees INT NULL COMMENT 'Nombre de participants attendus',
    setup_time INT NOT NULL DEFAULT 0 COMMENT 'Temps de préparation en minutes',
    cleanup_time INT NOT NULL DEFAULT 0 COMMENT 'Temps de nettoyage en minutes',
    notes VARCHAR(500) NULL COMMENT 'Notes additionnelles',
    
    -- Approbation
    approved_by BIGINT NULL COMMENT 'ID de l\'utilisateur qui a approuvé',
    approved_at DATETIME NULL COMMENT 'Date et heure d\'approbation',
    
    -- Annulation
    cancelled_by BIGINT NULL COMMENT 'ID de l\'utilisateur qui a annulé',
    cancelled_at DATETIME NULL COMMENT 'Date et heure d\'annulation',
    cancellation_reason VARCHAR(500) NULL COMMENT 'Raison de l\'annulation',
    
    -- Audit
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification',
    
    -- Contraintes
    CONSTRAINT fk_parent_reservation FOREIGN KEY (parent_reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
    CONSTRAINT chk_end_after_start CHECK (end_time > start_time),
    CONSTRAINT chk_setup_time_positive CHECK (setup_time >= 0),
    CONSTRAINT chk_cleanup_time_positive CHECK (cleanup_time >= 0),
    CONSTRAINT chk_expected_attendees_positive CHECK (expected_attendees IS NULL OR expected_attendees > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Table des réservations de ressources';

-- =====================================================
-- Index pour optimiser les performances
-- =====================================================

-- Index pour les recherches par ressource
CREATE INDEX idx_reservations_resource_id ON reservations(resource_id);

-- Index pour les recherches par utilisateur
CREATE INDEX idx_reservations_user_id ON reservations(user_id);

-- Index pour les recherches par cours
CREATE INDEX idx_reservations_course_id ON reservations(course_id);

-- Index pour les recherches par groupe de cours
CREATE INDEX idx_reservations_course_group_id ON reservations(course_group_id);

-- Index pour les recherches par statut
CREATE INDEX idx_reservations_status ON reservations(status);

-- Index pour les recherches par type
CREATE INDEX idx_reservations_type ON reservations(type);

-- Index pour les recherches par plage horaire
CREATE INDEX idx_reservations_time_range ON reservations(start_time, end_time);

-- Index pour les recherches par date de création
CREATE INDEX idx_reservations_created_at ON reservations(created_at);

-- Index composé pour les recherches de conflits
CREATE INDEX idx_reservations_conflict_check ON reservations(resource_id, start_time, end_time, status);

-- Index pour les réservations actives
CREATE INDEX idx_reservations_active ON reservations(status, start_time) 
WHERE status IN ('PENDING', 'CONFIRMED');

-- Index pour les réservations récurrentes
CREATE INDEX idx_reservations_recurring ON reservations(parent_reservation_id, recurring_pattern);

-- =====================================================
-- Données de test (optionnel)
-- =====================================================

-- Insertion de quelques réservations de test
INSERT INTO reservations (
    resource_id, user_id, title, description, start_time, end_time, 
    status, type, expected_attendees, setup_time, cleanup_time, notes
) VALUES 
-- Réservation de cours confirmée
(1, 1, 'Cours de Mathématiques', 'Cours de mathématiques niveau L1', 
 '2024-01-15 08:00:00', '2024-01-15 10:00:00', 
 'CONFIRMED', 'COURSE', 30, 15, 10, 'Prévoir projecteur et tableau'),

-- Réservation d'examen en attente
(2, 1, 'Examen Final Physique', 'Examen final de physique générale', 
 '2024-01-20 14:00:00', '2024-01-20 17:00:00', 
 'PENDING', 'EXAM', 50, 30, 15, 'Surveillance renforcée requise'),

-- Réservation de réunion confirmée
(3, 2, 'Réunion Conseil Pédagogique', 'Réunion mensuelle du conseil pédagogique', 
 '2024-01-18 10:00:00', '2024-01-18 12:00:00', 
 'CONFIRMED', 'MEETING', 15, 10, 5, 'Prévoir café et documents'),

-- Réservation d'événement
(1, 3, 'Conférence Innovation', 'Conférence sur l\'innovation technologique', 
 '2024-01-25 09:00:00', '2024-01-25 17:00:00', 
 'CONFIRMED', 'EVENT', 100, 60, 30, 'Événement toute la journée avec pause déjeuner'),

-- Réservation de maintenance
(4, 1, 'Maintenance Équipements', 'Maintenance préventive des équipements audiovisuels', 
 '2024-01-22 07:00:00', '2024-01-22 09:00:00', 
 'CONFIRMED', 'MAINTENANCE', NULL, 0, 0, 'Accès techniciens uniquement');

-- =====================================================
-- Vues utiles pour les requêtes fréquentes
-- =====================================================

-- Vue des réservations actives avec détails
CREATE OR REPLACE VIEW active_reservations AS
SELECT 
    r.*,
    TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time) as duration_minutes,
    DATE_SUB(r.start_time, INTERVAL r.setup_time MINUTE) as effective_start_time,
    DATE_ADD(r.end_time, INTERVAL r.cleanup_time MINUTE) as effective_end_time,
    CASE 
        WHEN r.recurring_pattern IS NOT NULL THEN 'Oui'
        ELSE 'Non'
    END as is_recurring
FROM reservations r
WHERE r.stessage;ccès!' as mé avec suinrm teviceation Serervation du Resnitialisd\'it LECT 'Scrip
SE=
================================================t
-- ====u scripin d
-- F=================================================
-- ====i;
de_ctf8mb4_unicoTE=ub4 COLLAARSET=utf8mCHDB DEFAULT NGINE=Inno
) Eat)d_ngeged_at (chahanudit_cEX idx_a  IND
  id),ation_d (reserv_iionit_reservat idx_audINDEXTAMP,
    T_TIMESFAULT CURREN DEIME NOT NULLged_at DATETchanT,
    TEXlue 
    new_vaalue TEXT,,
    old_vNOT NULLVARCHAR(50) type on_
    actiULL,BIGINT NOT Nvation_id    reser,
 RIMARY KEYNT PCREMENT AUTO_IN id BIGI
   og (dit_lervation_auresNOT EXISTS TABLE IF CREATE lle)
 (optionnee d'audit

-- TablIMITER ;
DEL
 //ENDD IF;
 );
    EN
       NOW()         tatus, 
     NEW.s
          tatus, .s       OLD', 
     NGEHAATUS_C  'ST        EW.id, 
          N
    UES (   ) VALt
     ed_a    chang      alue, 
         new_v
     ld_value,       ope, 
      tyaction_          
  _id, ationserv       re
     _log (n_audittioservaO re  INSERT INT      us THEN
at!= NEW.sttus OLD.sta
    IF nts importaut statments denges cha  -- Log deIN
  ROW
BEGOR EACH ervations
FresDATE ON AFTER UPn_changes
_reservatioR auditE TRIGGEons
CREATficatis modiit de'audr lger pou

-- Trig//
END ;IFEND 
    eures';r 12 hdépassepeut pas e n nservatio de réLa duréeXT = 'GE_TE MESSA'45000' SETSTATE AL SQL  SIGNEN
      12 THime) >  NEW.end_t.start_time,, NEWF(HOURMPDIF IF TIMESTAres)
    (12 heue maximaleduréValider la    
    -- F;
    END I
 passé';s le n danatio une réservble de créersiposTEXT = 'ImSSAGE_ET ME' S '45000ATEL SQLSTNA     SIG
    NOW() THEN <meEW.start_tiNCE' AND N= 'MAINTENAe !W.typNEF 
    Intenance)(sauf maiassé dans le pest pas tion n'réservae la  Valider qu 
    -- IF;
   
    ENDde début';eure \'h après l être de fin doiteure'L\'h = AGE_TEXTESS00' SET MSTATE '450SQL    SIGNAL  THEN
    t_timeNEW.star= e <im_tendW.
    IF NEs le début aprè fin estla que  Valider   --BEGIN
 EACH ROW
R vations
FON reserRE INSERT Oert
BEFO_instion_beforeate_reservaRIGGER validn
CREATE Tioinsertvant es aer les donnéidr pour val

-- Trigge //
DELIMITER======
==============================================on
-- =la validatiet it  l'auders pour==
-- Trigg=================================================
-- == ;
ERITIM
DELD //
ate;
ENon_datiER BY reservme)
    ORDart_tiE(stBY DATOUP 
    GR_end_datedate AND pEN p_start_) BETWEE(start_timeDATND '
      ANFIRMEDus = 'CO    AND statd
  p_resource_iid = urce_soRE res
    WHEvationM reser   FRO_time
 reservations last_d_time) aen       MAX(e,
 ion_timrst_reservate) as fitart_tim      MIN(s
  nutes,uration_mis avg_de)) atimend_e, imrt_tNUTE, staSTAMPDIFF(MI    AVG(TIMEved,
    nutes_reseral_mime)) as totend_tie,  start_timIFF(MINUTE,PDAMUM(TIMEST        Sations,
rvl_rese as tota COUNT(*)  
     n_date,reservatioime) as _trtDATE(sta     CT 
     SELE
  BEGINATE
)
end_date D p_INTE,
    date DAt_tar   IN p_s
 T,GINd BIp_resource_iN tats(
    IlizationSeUtiResourcEDURE GetE PROCation
CREATlisti'uques datistir les stour obtenidure p-- Procé/

 );
END /   e_start)
  effectivUTE) >  MINleanup_timeERVAL r.cme, INTDD(r.end_tiATE_A    (D      ND 
         And)
 ve_eectiffTE) < ee MINUup_tim r.setme, INTERVALr.start_tiATE_SUB(        (D   AND (
  
   _id)ioneservatclude_r.id != p_exULL OR ron_id IS Natilude_reserv AND (p_excD')
     , 'CONFIRMENG'DI ('PEN INtatusAND r.sid
      urce_id = p_resoresource_ WHERE r.   s r
eservation r  FROMr.*
  SELECT   ;
    
  E)p_time MINUTleanuERVAL p_ctime, INTd_enDD(p_nd = DATE_Afective_e   SET ef MINUTE);
 timeAL p_setup_me, INTERVart_tiATE_SUB(p_stve_start = DSET effecti
    
    ETIME;e_end DATE effectiv
    DECLARrt DATETIME;ctive_staffeRE e  DECLA)
BEGIN
  T
ion_id BIGINreservatclude__ex
    IN p_time INT,p_cleanup IN ,
   up_time INT    IN p_set,
ATETIME_time DN p_endTIME,
    IDATE_time IN p_start,
    d BIGINTurce_i   IN p_reso(
 lictsonfeservationCheckR PROCEDURE CCREATEonflits
 les cerour vérifiédure p

-- ProcTER //IMI

DEL=============================================
-- ========leskées utis stocureocéd=====
-- Pr================================================
-- 
_time;R BY r.startRDE
O = CURDATE()time)d_r.enATE(OR D   )
) = CURDATE(start_timeE DATE(r.ons r
WHERrvatiM rese_status
FRO as current'
    ENDerminée'TE LS     E venir'
   () THEN 'À NOW >start_timeWHEN r.      rs'
  HEN 'En cou) TNOW(me > .end_ti NOW() AND rtime <=EN r.start_    WH
    ASE    C    r.*,
 SELECT 
S
ions Areservaty_ VIEW todaR REPLACETE OCREA du jour
rvationsVue des rése
-- 
urce_id; BY resoons
GROUPrvati resevation
FROMserast_reted_at) as lrea
    MAX(cervation,as first_reseated_at) 
    MIN(crion_minutes,g_durat as av))timee, end_ start_timUTE,DIFF(MINTAMPAVG(TIMESs,
    reservationelled_s cancHEN 1 END) aCANCELLED' Ttus = 'ta sT(CASE WHENOUN Cions,
   ng_reservatD) as pendiG' THEN 1 EN= 'PENDINN status SE WHE   COUNT(CArvations,
 _resenfirmedND) as co' THEN 1 EEDs = 'CONFIRMtuSE WHEN sta   COUNT(CAons,
 _reservatitotal*) as  COUNT(,
   urce_idesoECT 
    rce AS
SELs_by_resourn_statW reservatioVIE REPLACE ATE ORource
CREpar resses tistiqudes sta Vue time;

--Y r.start_')
ORDER B'CONFIRMEDNDING', N ('PEatus I