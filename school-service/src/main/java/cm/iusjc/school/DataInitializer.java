package cm.iusjc.school;

import cm.iusjc.school.entity.Filiere;
import cm.iusjc.school.entity.Groupe;
import cm.iusjc.school.entity.Niveau;
import cm.iusjc.school.entity.School;
import cm.iusjc.school.repository.FiliereRepository;
import cm.iusjc.school.repository.GroupeRepository;
import cm.iusjc.school.repository.NiveauRepository;
import cm.iusjc.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SchoolRepository  schoolRepository;
    private final FiliereRepository filiereRepository;
    private final NiveauRepository  niveauRepository;
    private final GroupeRepository  groupeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (schoolRepository.count() > 0) {
            log.info("[DataInitializer] Schools already seeded — skipping.");
            return;
        }

        log.info("[DataInitializer] Seeding schools, filieres, niveaux, groupes...");

        School sji       = school("Saint Jean Ingénieur",                     "SJI",       "Yaoundé");
        School sjm       = school("Saint Jean Management",                    "SJM",       "Yaoundé");
        School prepavogt = school("Prépavogt",                                "PRÉPAVOGT", "Yaoundé");
        School cpge      = school("Classes Préparatoires aux Grandes Écoles", "CPGE",      "Yaoundé");

        // SJI
        Filiere gi = filiere("Génie Informatique", "GI", sji);
        Filiere gc = filiere("Génie Civil",        "GC", sji);

        Niveau giL1 = niveau("L1", "L1", 1, gi);
        Niveau giL2 = niveau("L2", "L2", 2, gi);
        Niveau gcL1 = niveau("L1", "L1", 1, gc);

        groupe("GI L1 Groupe A", "GI-L1-A", 35, giL1);
        groupe("GI L1 Groupe B", "GI-L1-B", 35, giL1);
        groupe("GI L2 Groupe A", "GI-L2-A", 35, giL2);
        groupe("GC L1 Groupe A", "GC-L1-A", 35, gcL1);

        // SJM
        Filiere mgmt    = filiere("Management", "ME", sjm);
        Filiere finance = filiere("Finance",    "FC", sjm);

        Niveau meL1 = niveau("L1", "L1", 1, mgmt);
        Niveau fcL2 = niveau("L2", "L2", 2, finance);

        groupe("ME L1 Groupe A", "ME-L1-A", 35, meL1);
        groupe("ME L1 Groupe B", "ME-L1-B", 35, meL1);
        groupe("FC L2 Groupe A", "FC-L2-A", 35, fcL2);

        // PRÉPAVOGT
        Filiere pvMath = filiere("Mathématiques", "MATH-PV", prepavogt);
        Niveau  pvP1   = niveau("Prépa 1", "P1", 1, pvMath);
        groupe("Prépa 1 Gr. A", "PV-P1-A", 30, pvP1);

        // CPGE
        Filiere cpgeMath = filiere("Mathématiques", "MATH-CG", cpge);
        Niveau  cpge1    = niveau("CPGE 1", "C1", 1, cpgeMath);
        groupe("CPGE 1 MPSI", "CG-C1-A", 30, cpge1);

        log.info("[DataInitializer] Done — 4 schools, filieres, niveaux, groupes seeded.");
    }

    private School school(String name, String code, String city) {
        School s = new School();
        s.setName(name); s.setCode(code); s.setCity(city);
        s.setCountry("Cameroun"); s.setActive(true);
        // Couleurs distinctes par école pour le frontend
        String[] palette = {"#1D4ED8", "#15803D", "#DC2626", "#7C3AED", "#EA580C"};
        long count = schoolRepository.count();
        s.setCouleur(palette[(int)(count % palette.length)]);
        return schoolRepository.save(s);
    }

    private Filiere filiere(String name, String code, School school) {
        Filiere f = new Filiere();
        f.setName(name); f.setCode(code); f.setSchool(school); f.setActive(true);
        return filiereRepository.save(f);
    }

    private Niveau niveau(String name, String code, int ordre, Filiere filiere) {
        Niveau n = new Niveau();
        n.setName(name); n.setCode(code); n.setOrdre(ordre); n.setFiliere(filiere); n.setActive(true);
        return niveauRepository.save(n);
    }

    private void groupe(String name, String code, int capacite, Niveau niveau) {
        Groupe g = new Groupe();
        g.setName(name); g.setCode(code); g.setCapacite(capacite); g.setNiveau(niveau); g.setActive(true);
        groupeRepository.save(g);
    }
}
