package cm.iusjc.teacheravailability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class TeacherAvailabilityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherAvailabilityServiceApplication.class, args);
    }
}