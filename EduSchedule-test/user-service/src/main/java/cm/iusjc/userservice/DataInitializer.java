package cm.iusjc.userservice;

import cm.iusjc.userservice.entity.Role;
import cm.iusjc.userservice.entity.User;
import cm.iusjc.userservice.repository.RoleRepository;
import cm.iusjc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    // BCrypt hash of "admin123" — verified with BCryptPasswordEncoder.matches()
    private static final String HASH = "$2a$10$ZvIEi4Q2LqUh4fX0pGPGzemMwvSRjwt5nwxTH/mJjcqPscDNUKvUi";

    @Override
    public void run(ApplicationArguments args) {
        boolean rolesExist = roleRepository.count() > 0;
        boolean usersExist = userRepository.count() > 0;

        if (rolesExist && usersExist) {
            log.info("[DataInitializer] Roles and users already seeded — skipping.");
            return;
        }

        log.info("[DataInitializer] Seeding roles and users...");

        Role adminRole   = rolesExist ? roleRepository.findByName("ADMIN").orElseGet(() -> createRole("ADMIN", "Administrateur système"))
                                      : createRole("ADMIN", "Administrateur système");
        Role teacherRole = rolesExist ? roleRepository.findByName("TEACHER").orElseGet(() -> createRole("TEACHER", "Enseignant"))
                                      : createRole("TEACHER", "Enseignant");
        Role studentRole = rolesExist ? roleRepository.findByName("STUDENT").orElseGet(() -> createRole("STUDENT", "Étudiant"))
                                      : createRole("STUDENT", "Étudiant");

        createUser("admin",    "Admin",    "Système",  "admin@iusjc.cm",    adminRole);
        createUser("teacher1", "Alain",    "Mbarga",   "alain@iusjc.cm",    teacherRole);
        createUser("teacher2", "Pierre",   "Essama",   "pierre@iusjc.cm",   teacherRole);
        createUser("teacher3", "Samuel",   "Nkoa",     "samuel@iusjc.cm",   teacherRole);
        createUser("teacher4", "Marie",    "Ateba",    "marie@iusjc.cm",    teacherRole);
        createUser("teacher5", "Robert",   "Nganou",   "robert@iusjc.cm",   teacherRole);
        createUser("teacher6", "Paul",     "Essomba",  "paul@iusjc.cm",     teacherRole);
        createUser("teacher7", "Helene",   "Mbarga",   "helene@iusjc.cm",   teacherRole);
        createUser("teacher8", "Sylvie",   "Biya",     "sylvie@iusjc.cm",   teacherRole);
        createUser("teacher9", "Cedric",   "Owona",    "cedric@iusjc.cm",   teacherRole);
        createUser("student1", "Etudiant", "Un",       "student1@iusjc.cm", studentRole);

        log.info("[DataInitializer] Done — 3 roles, 11 users created.");
    }

    private Role createRole(String name, String description) {
        Role r = new Role();
        r.setName(name);
        r.setDescription(description);
        r.setActive(true);
        return roleRepository.save(r);
    }

    private void createUser(String username, String firstName, String lastName, String email, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.info("[DataInitializer] User '{}' already exists — skipping.", username);
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(HASH);
        u.setRole(role);
        u.setEnabled(true);
        u.setAccountNonExpired(true);
        u.setAccountNonLocked(true);
        u.setCredentialsNonExpired(true);
        userRepository.save(u);
    }
}
