package cm.iusjc.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Extrait une liste depuis une réponse qui peut être :
     *  - directement une List<Map>
     *  - un objet paginé { content: [...] }
     *  - un objet enveloppé  { data: [...] }
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Object body) {
        if (body instanceof List) return (List<Map<String, Object>>) body;
        if (body instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) body;
            Object content = map.get("content");
            if (content instanceof List) return (List<Map<String, Object>>) content;
            Object data = map.get("data");
            if (data instanceof List) return (List<Map<String, Object>>) data;
        }
        return List.of();
    }

    /** Appel générique : essaie de désérialiser en List ou en Map selon la réponse */
    private Mono<List<Map<String, Object>>> fetchList(String url) {
        WebClient client = webClientBuilder.build();
        // On tente d'abord comme Map (paginé / enveloppé)
        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Object>() {})
                .timeout(Duration.ofSeconds(12))
                .map(this::extractList)
                .onErrorResume(e -> {
                    log.warn("Could not fetch {}: {}", url, e.getMessage());
                    return Mono.just(List.of());
                });
    }

    // ── Collecte par domaine ──────────────────────────────────────────────────

    public Mono<List<Map<String, Object>>> collectUserData() {
        return fetchList(userServiceUrl + "/api/users");
    }

    public Mono<Map<String, Object>> collectUserStatistics() {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/users/statistics")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(Map.of());
    }

    public Mono<List<Map<String, Object>>> collectCourseData() {
        // Le course-service expose /api/v1/courses (liste paginée ou directe)
        return fetchList(courseServiceUrl + "/api/v1/courses");
    }

    public Mono<List<Map<String, Object>>> collectReservationData() {
        return fetchList(reservationServiceUrl + "/api/v1/reservations");
    }

    public Mono<List<Map<String, Object>>> collectScheduleData() {
        // Le scheduling-service expose /api/v1/schedules
        return fetchList(schedulingServiceUrl + "/api/v1/schedules");
    }

    public Mono<List<Map<String, Object>>> collectResourceData() {
        // Le resource-service expose /api/v1/salles (salles = rooms)
        return fetchList(resourceServiceUrl + "/api/v1/salles");
    }

    // ── Collecte globale ──────────────────────────────────────────────────────

    /**
     * Collecte toutes les données en parallèle.
     * Chaque appel est indépendant : un échec partiel ne bloque pas les autres.
     */
    public Mono<Map<String, Object>> collectAllData() {
        return Mono.zip(
                collectUserData(),
                collectCourseData(),
                collectReservationData(),
                collectScheduleData(),
                collectResourceData()
        ).map(tuple -> {
            Map<String, Object> result = new HashMap<>();
            result.put("users",        tuple.getT1());
            result.put("courses",      tuple.getT2());
            result.put("reservations", tuple.getT3());
            result.put("schedules",    tuple.getT4());
            result.put("resources",    tuple.getT5());
            log.info("Data collected — users:{} courses:{} reservations:{} schedules:{} resources:{}",
                    tuple.getT1().size(), tuple.getT2().size(),
                    tuple.getT3().size(), tuple.getT4().size(), tuple.getT5().size());
            return result;
        }).onErrorResume(e -> {
            log.error("Error collecting all data: {}", e.getMessage());
            return Mono.just(Map.of());
        });
    }
}
