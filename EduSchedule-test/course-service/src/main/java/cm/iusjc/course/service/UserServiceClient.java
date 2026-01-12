package cm.iusjc.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.user-service.url:http://localhost:8081}")
    private String userServiceUrl;
    
    public String getUserName(Long userId) {
        try {
            // Appel au User Service pour récupérer le nom de l'utilisateur
            String url = userServiceUrl + "/api/users/" + userId + "/name";
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.warn("Failed to fetch user name for ID: {}. Error: {}", userId, e.getMessage());
            return "Utilisateur #" + userId;
        }
    }
    
    public boolean isTeacher(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId + "/role";
            String role = restTemplate.getForObject(url, String.class);
            return "TEACHER".equals(role) || "ADMIN".equals(role);
        } catch (Exception e) {
            log.warn("Failed to check user role for ID: {}. Error: {}", userId, e.getMessage());
            return false;
        }
    }
}