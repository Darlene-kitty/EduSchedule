package cm.iusjc.ent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EntIntegrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntIntegrationServiceApplication.class, args);
    }
}
