package cm.iusjc.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FrontendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}

@Controller
class HomeController {
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
