package cm.iusjc.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCollectionService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.user-service.url}")
    private String userServiceUrl;
    
    @Value("${app.course-service.url}")
    private String courseServiceUrl;
    
    @Value("${app.reservation-service.url}")
    private String reservationServiceUrl;
    
    @Value("${app.scheduling-service.url}")
    private String schedulingServiceUrl;
    
    @Value("${app.resource-service.url}")
    private String resourceServiceUrl;
    
    /**
     * Collecte les données des utilisateurs
     */
    public Mono<List<Map<String, Object>>> collectUserData() {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/users")
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting user data: {}", error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Collecte les statistiques des utilisateurs
     */
    public Mono<Map<String, Object>> collectUserStatistics() {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/users/statistics")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting user statistics: {}", error.getMessage()))
                .onErrorReturn(Map.of());
    }
    
    /**
     * Collecte les données des cours
     */
    public Mono<List<Map<String, Object>>> collectCourseData() {
        return webClientBuilder.build()
                .get()
                .uri(courseServiceUrl + "/api/v1/cours")
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting course data: {}", error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Collecte les données des réservations
     */
    public Mono<List<Map<String, Object>>> collectReservationData() {
        return webClientBuilder.build()
                .get()
                .uri(reservationServiceUrl + "/api/reservations")
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting reservation data: {}", error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Collecte les données des emplois du temps
     */
    public Mono<List<Map<String, Object>>> collectScheduleData() {
        return webClientBuilder.build()
                .get()
                .uri(schedulingServiceUrl + "/api/v1/emplois")
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting schedule data: {}", error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Collecte les données des ressources
     */
    public Mono<List<Map<String, Object>>> collectResourceData() {
        return webClientBuilder.build()
                .get()
                .uri(resourceServiceUrl + "/api/v1/resources")
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error collecting resource data: {}", error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Collecte toutes les données nécessaires pour les statistiques
     */
    public Mono<Map<String, Object>> collectAllData() {
        return Mono.zip(
                collectUserData(),
                collectCourseData(),
                collectReservationData(),
                collectScheduleData(),
                collectResourceData()
        ).map(tuple -> {
            return Map.of(
                "users", tuple.getT1(),
                "courses", tuple.getT2(),
                "reservations", tuple.getT3(),
                "schedules", tuple.getT4(),
                "resources", tuple.getT5()
            );
        }).doOnError(error -> log.error("Error collecting all data: {}", error.getMessage()))
        .onErrorReturn(Map.of());
    }
}