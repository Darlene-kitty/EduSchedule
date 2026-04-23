package cm.iusjc.teacheravailabilityservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Component;

@FeignClient(name = "user-service", fallback = UserServiceClient.UserServiceFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}/name")
    String getUserName(@PathVariable("id") Long id);
    
    @GetMapping("/api/users/{id}/role")
    String getUserRole(@PathVariable("id") Long id);
    
    @GetMapping("/api/users/{id}/email")
    String getUserEmail(@PathVariable("id") Long id);
    
    @Component
    static class UserServiceFallback implements UserServiceClient {
        
        @Override
        public String getUserName(Long id) {
            return "Enseignant " + id; // Fallback générique
        }
        
        @Override
        public String getUserRole(Long id) {
            return "TEACHER"; // Fallback par défaut
        }
        
        @Override
        public String getUserEmail(Long id) {
            return "teacher" + id + "@example.com"; // Fallback générique
        }
    }
}