package cm.iusjc.resource;

import cm.iusjc.resource.entity.*;
import cm.iusjc.resource.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initialise les données de référence du resource-service :
 * types de matériels, matériels, salles et inventaire salle↔matériel.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TypeMaterielRepository typeMaterielRepo;
    private final MaterielRepository materielRepo;
    private final SalleRepository salleRepo;
    private final SalleMaterielRepository salleMaterielRepo;

    @Override
    public void run(String... args) {
        if (typeMaterielRepo.count() == 0) {
            initTypesMateriels();
        }
        if (materielRepo.count() == 0) {
            initMateriels();
        }
        if (salleRepo.count() == 0) {
            initSalles();
        }
        if (salleMaterielRepo.count() == 0) {
            initInventaireSalles();
        }
        log.info("DataInitializer resource-service terminé.");
    }

    // ── Types de matériels ────────────────────────────────────────────────────

    private void initTypesMateriels() {
        log.info("Initialisation des types de matériels...");
        List<TypeMateriel> types = List.of(
            type("PROJECTEUR",    "Projecteur / Vidéoprojecteur", "videocam",       "#1D4ED8"),
            type("ORDINATEUR",    "Ordinateur / PC",              "computer",        "#0F766E"),
            type("TABLEAU_BLANC", "Tableau blanc / Ardoise",      "edit",            "#7C3AED"),
            type("SONO",          "Système sonore / Baffles",     "volume_up",       "#B45309"),
            type("MICRO",         "Microphone",                   "mic",             "#DC2626"),
            type("CLIMATISATION", "Climatisation / Ventilation",  "ac_unit",         "#0369A1"),
            type("IMPRIMANTE",    "Imprimante / Scanner",         "print",           "#4B5563"),
            type("CAMERA",        "Caméra / Webcam",              "camera_alt",      "#6D28D9"),
            type("TABLEAU_INTER", "Tableau interactif",           "touch_app",       "#065F46"),
            type("AUTRE",         "Autre équipement",             "devices_other",   "#6B7280")
        );
        typeMaterielRepo.saveAll(types);
        log.info("{} types de matériels créés.", types.size());
    }

    private TypeMateriel type(String code, String nom, String icone, String couleur) {
        TypeMateriel t = new TypeMateriel();
        t.setCode(code);
        t.setNom(nom);
        t.setIcone(icone);
        t.setCouleur(couleur);
        t.setActive(true);
        return t;
    }

    // ── Matériels ─────────────────────────────────────────────────────────────

    private void initMateriels() {
        log.info("Initialisation des matériels...");
        TypeMateriel proj   = typeMaterielRepo.findByCode("PROJECTEUR").orElse(null);
        TypeMateriel ordi   = typeMaterielRepo.findByCode("ORDINATEUR").orElse(null);
        TypeMateriel tab    = typeMaterielRepo.findByCode("TABLEAU_BLANC").orElse(null);
        TypeMateriel sono   = typeMaterielRepo.findByCode("SONO").orElse(null);
        TypeMateriel micro  = typeMaterielRepo.findByCode("MICRO").orElse(null);
        TypeMateriel clim   = typeMaterielRepo.findByCode("CLIMATISATION").orElse(null);
        TypeMateriel tabInt = typeMaterielRepo.findByCode("TABLEAU_INTER").orElse(null);

        List<Materiel> materiels = List.of(
            // Projecteurs
            materiel("PROJ-001", "Projecteur Epson EB-X51",   proj,   "Epson",  "EB-X51",  "SN-PROJ-001", "IUSJC", "A101", Materiel.EtatMateriel.BON_ETAT),
            materiel("PROJ-002", "Projecteur Epson EB-X51",   proj,   "Epson",  "EB-X51",  "SN-PROJ-002", "IUSJC", "A102", Materiel.EtatMateriel.BON_ETAT),
            materiel("PROJ-003", "Projecteur BenQ MX550",     proj,   "BenQ",   "MX550",   "SN-PROJ-003", "IUSJC", "B201", Materiel.EtatMateriel.BON_ETAT),
            materiel("PROJ-004", "Projecteur BenQ MX550",     proj,   "BenQ",   "MX550",   "SN-PROJ-004", "IUSJC", "AMPHI-A", Materiel.EtatMateriel.BON_ETAT),
            materiel("PROJ-005", "Projecteur Optoma HD28HDR", proj,   "Optoma", "HD28HDR", "SN-PROJ-005", "IUSJC", "AMPHI-B", Materiel.EtatMateriel.BON_ETAT),
            // Ordinateurs
            materiel("PC-001",   "PC HP ProDesk 400",         ordi,   "HP",     "ProDesk 400", "SN-PC-001", "IUSJC", "TP-INFO-1", Materiel.EtatMateriel.BON_ETAT),
            materiel("PC-002",   "PC HP ProDesk 400",         ordi,   "HP",     "ProDesk 400", "SN-PC-002", "IUSJC", "TP-INFO-1", Materiel.EtatMateriel.BON_ETAT),
            materiel("PC-003",   "PC HP ProDesk 400",         ordi,   "HP",     "ProDesk 400", "SN-PC-003", "IUSJC", "TP-INFO-1", Materiel.EtatMateriel.BON_ETAT),
            materiel("PC-004",   "PC Dell OptiPlex 3080",     ordi,   "Dell",   "OptiPlex 3080", "SN-PC-004", "IUSJC", "TP-INFO-2", Materiel.EtatMateriel.BON_ETAT),
            materiel("PC-005",   "PC Dell OptiPlex 3080",     ordi,   "Dell",   "OptiPlex 3080", "SN-PC-005", "IUSJC", "TP-INFO-2", Materiel.EtatMateriel.BON_ETAT),
            materiel("PC-006",   "PC Dell OptiPlex 3080",     ordi,   "Dell",   "OptiPlex 3080", "SN-PC-006", "IUSJC", "TP-INFO-2", Materiel.EtatMateriel.USAGE),
            // Tableaux blancs
            materiel("TAB-001",  "Tableau blanc 120x90",      tab,    "Nobo",   "Classic",  "SN-TAB-001", "IUSJC", "A101", Materiel.EtatMateriel.BON_ETAT),
            materiel("TAB-002",  "Tableau blanc 120x90",      tab,    "Nobo",   "Classic",  "SN-TAB-002", "IUSJC", "A102", Materiel.EtatMateriel.BON_ETAT),
            materiel("TAB-003",  "Tableau blanc 150x100",     tab,    "Nobo",   "Premium",  "SN-TAB-003", "IUSJC", "B201", Materiel.EtatMateriel.BON_ETAT),
            materiel("TAB-004",  "Tableau blanc 150x100",     tab,    "Nobo",   "Premium",  "SN-TAB-004", "IUSJC", "AMPHI-A", Materiel.EtatMateriel.BON_ETAT),
            // Systèmes sonores
            materiel("SONO-001", "Système sono JBL EON610",   sono,   "JBL",    "EON610",   "SN-SONO-001", "IUSJC", "AMPHI-A", Materiel.EtatMateriel.BON_ETAT),
            materiel("SONO-002", "Système sono JBL EON610",   sono,   "JBL",    "EON610",   "SN-SONO-002", "IUSJC", "AMPHI-B", Materiel.EtatMateriel.BON_ETAT),
            // Microphones
            materiel("MICRO-001","Micro sans fil Shure BLX",  micro,  "Shure",  "BLX24",    "SN-MICRO-001", "IUSJC", "AMPHI-A", Materiel.EtatMateriel.BON_ETAT),
            materiel("MICRO-002","Micro sans fil Shure BLX",  micro,  "Shure",  "BLX24",    "SN-MICRO-002", "IUSJC", "AMPHI-B", Materiel.EtatMateriel.BON_ETAT),
            // Climatisation
            materiel("CLIM-001", "Climatiseur Daikin 18000BTU", clim, "Daikin", "FTXS18",  "SN-CLIM-001", "IUSJC", "A101", Materiel.EtatMateriel.BON_ETAT),
            materiel("CLIM-002", "Climatiseur Daikin 18000BTU", clim, "Daikin", "FTXS18",  "SN-CLIM-002", "IUSJC", "A102", Materiel.EtatMateriel.BON_ETAT),
            // Tableaux interactifs
            materiel("TABINT-001","Tableau interactif Smart 75\"", tabInt, "Smart", "SBID-7275", "SN-TABINT-001", "IUSJC", "TP-INFO-1", Materiel.EtatMateriel.BON_ETAT)
        );
        materielRepo.saveAll(materiels);
        log.info("{} matériels créés.", materiels.size());
    }

    private Materiel materiel(String code, String nom, TypeMateriel type, String marque,
                               String modele, String serie, String ecole, String salle,
                               Materiel.EtatMateriel etat) {
        Materiel m = new Materiel();
        m.setCode(code);
        m.setNom(nom);
        m.setTypeMateriel(type);
        m.setMarque(marque);
        m.setModele(modele);
        m.setNumeroSerie(serie);
        m.setEcole(ecole);
        m.setSalle(salle);
        m.setEtat(etat);
        m.setDateAcquisition("2023-09-01");
        m.setActive(true);
        return m;
    }

    // ── Salles ────────────────────────────────────────────────────────────────

    private void initSalles() {
        log.info("Initialisation des salles...");
        List<Salle> salles = List.of(
            salle("A101",     "Salle A101",          "Bâtiment A", "1", 40,  Salle.TypeSalle.SALLE_TD),
            salle("A102",     "Salle A102",          "Bâtiment A", "1", 40,  Salle.TypeSalle.SALLE_TD),
            salle("B201",     "Salle B201",          "Bâtiment B", "2", 35,  Salle.TypeSalle.SALLE_COURS),
            salle("B202",     "Salle B202",          "Bâtiment B", "2", 35,  Salle.TypeSalle.SALLE_COURS),
            salle("AMPHI-A",  "Amphithéâtre A",      "Bâtiment C", "0", 200, Salle.TypeSalle.AMPHITHEATRE),
            salle("AMPHI-B",  "Amphithéâtre B",      "Bâtiment C", "0", 150, Salle.TypeSalle.AMPHITHEATRE),
            salle("TP-INFO-1","Salle TP Informatique 1", "Bâtiment D", "1", 30, Salle.TypeSalle.SALLE_TP),
            salle("TP-INFO-2","Salle TP Informatique 2", "Bâtiment D", "1", 30, Salle.TypeSalle.SALLE_TP),
            salle("LAB-PHYS", "Laboratoire Physique", "Bâtiment E", "0", 25, Salle.TypeSalle.LABORATOIRE),
            salle("BIBLIO",   "Bibliothèque",         "Bâtiment F", "0", 80, Salle.TypeSalle.BIBLIOTHEQUE)
        );
        salleRepo.saveAll(salles);
        log.info("{} salles créées.", salles.size());
    }

    private Salle salle(String code, String name, String batiment, String etage,
                         int capacite, Salle.TypeSalle type) {
        Salle s = new Salle();
        s.setCode(code);
        s.setName(name);
        s.setBatiment(batiment);
        s.setEtage(etage);
        s.setCapacite(capacite);
        s.setType(type);
        s.setDisponible(true);
        s.setActive(true);
        return s;
    }

    // ── Inventaire Salle ↔ Matériel ───────────────────────────────────────────

    private void initInventaireSalles() {
        log.info("Initialisation de l'inventaire salle↔matériel...");

        // Récupérer les salles et matériels par code
        Salle a101    = salleRepo.findByCode("A101").orElse(null);
        Salle a102    = salleRepo.findByCode("A102").orElse(null);
        Salle b201    = salleRepo.findByCode("B201").orElse(null);
        Salle amphiA  = salleRepo.findByCode("AMPHI-A").orElse(null);
        Salle amphiB  = salleRepo.findByCode("AMPHI-B").orElse(null);
        Salle tpInfo1 = salleRepo.findByCode("TP-INFO-1").orElse(null);
        Salle tpInfo2 = salleRepo.findByCode("TP-INFO-2").orElse(null);

        Materiel proj1   = materielRepo.findByCode("PROJ-001").orElse(null);
        Materiel proj2   = materielRepo.findByCode("PROJ-002").orElse(null);
        Materiel proj3   = materielRepo.findByCode("PROJ-003").orElse(null);
        Materiel proj4   = materielRepo.findByCode("PROJ-004").orElse(null);
        Materiel proj5   = materielRepo.findByCode("PROJ-005").orElse(null);
        Materiel pc1     = materielRepo.findByCode("PC-001").orElse(null);
        Materiel pc2     = materielRepo.findByCode("PC-002").orElse(null);
        Materiel pc3     = materielRepo.findByCode("PC-003").orElse(null);
        Materiel pc4     = materielRepo.findByCode("PC-004").orElse(null);
        Materiel pc5     = materielRepo.findByCode("PC-005").orElse(null);
        Materiel pc6     = materielRepo.findByCode("PC-006").orElse(null);
        Materiel tab1    = materielRepo.findByCode("TAB-001").orElse(null);
        Materiel tab2    = materielRepo.findByCode("TAB-002").orElse(null);
        Materiel tab3    = materielRepo.findByCode("TAB-003").orElse(null);
        Materiel tab4    = materielRepo.findByCode("TAB-004").orElse(null);
        Materiel sono1   = materielRepo.findByCode("SONO-001").orElse(null);
        Materiel sono2   = materielRepo.findByCode("SONO-002").orElse(null);
        Materiel micro1  = materielRepo.findByCode("MICRO-001").orElse(null);
        Materiel micro2  = materielRepo.findByCode("MICRO-002").orElse(null);
        Materiel tabInt1 = materielRepo.findByCode("TABINT-001").orElse(null);

        // Salle A101 (TD) : projecteur + tableau blanc
        if (a101 != null) {
            if (proj1 != null) salleMaterielRepo.save(sm(a101, proj1, 1, true, "Projecteur fixe au plafond"));
            if (tab1  != null) salleMaterielRepo.save(sm(a101, tab1,  1, true, "Tableau blanc mural"));
        }
        // Salle A102 (TD) : projecteur + tableau blanc
        if (a102 != null) {
            if (proj2 != null) salleMaterielRepo.save(sm(a102, proj2, 1, true, "Projecteur fixe au plafond"));
            if (tab2  != null) salleMaterielRepo.save(sm(a102, tab2,  1, true, "Tableau blanc mural"));
        }
        // Salle B201 (Cours) : projecteur + tableau blanc
        if (b201 != null) {
            if (proj3 != null) salleMaterielRepo.save(sm(b201, proj3, 1, true, "Projecteur mobile"));
            if (tab3  != null) salleMaterielRepo.save(sm(b201, tab3,  1, true, "Tableau blanc mural"));
        }
        // Amphi A : projecteur + sono + micro + tableau blanc
        if (amphiA != null) {
            if (proj4  != null) salleMaterielRepo.save(sm(amphiA, proj4,  1, true, "Projecteur principal"));
            if (tab4   != null) salleMaterielRepo.save(sm(amphiA, tab4,   1, true, "Tableau blanc"));
            if (sono1  != null) salleMaterielRepo.save(sm(amphiA, sono1,  2, true, "Système sono stéréo"));
            if (micro1 != null) salleMaterielRepo.save(sm(amphiA, micro1, 2, true, "Micros sans fil"));
        }
        // Amphi B : projecteur + sono + micro
        if (amphiB != null) {
            if (proj5  != null) salleMaterielRepo.save(sm(amphiB, proj5,  1, true, "Projecteur principal"));
            if (sono2  != null) salleMaterielRepo.save(sm(amphiB, sono2,  2, true, "Système sono stéréo"));
            if (micro2 != null) salleMaterielRepo.save(sm(amphiB, micro2, 2, true, "Micros sans fil"));
        }
        // TP Info 1 : ordinateurs + tableau interactif + projecteur
        if (tpInfo1 != null) {
            if (pc1     != null) salleMaterielRepo.save(sm(tpInfo1, pc1,     1, true, "Poste étudiant 1"));
            if (pc2     != null) salleMaterielRepo.save(sm(tpInfo1, pc2,     1, true, "Poste étudiant 2"));
            if (pc3     != null) salleMaterielRepo.save(sm(tpInfo1, pc3,     1, true, "Poste étudiant 3"));
            if (tabInt1 != null) salleMaterielRepo.save(sm(tpInfo1, tabInt1, 1, true, "Tableau interactif mural"));
        }
        // TP Info 2 : ordinateurs + projecteur
        if (tpInfo2 != null) {
            if (pc4 != null) salleMaterielRepo.save(sm(tpInfo2, pc4, 1, true, "Poste étudiant 1"));
            if (pc5 != null) salleMaterielRepo.save(sm(tpInfo2, pc5, 1, true, "Poste étudiant 2"));
            if (pc6 != null) salleMaterielRepo.save(sm(tpInfo2, pc6, 1, false, "Poste étudiant 3 (usagé)"));
        }

        log.info("Inventaire salle↔matériel initialisé.");
    }

    private SalleMateriel sm(Salle salle, Materiel materiel, int quantite, boolean requis, String notes) {
        SalleMateriel sm = new SalleMateriel();
        sm.setSalle(salle);
        sm.setMateriel(materiel);
        sm.setQuantiteTotale(quantite);
        sm.setQuantiteDisponible(quantite);
        sm.setQuantiteReservee(0);
        sm.setRequis(requis);
        sm.setNotes(notes);
        sm.setDateInstallation(LocalDateTime.now());
        return sm;
    }
}
