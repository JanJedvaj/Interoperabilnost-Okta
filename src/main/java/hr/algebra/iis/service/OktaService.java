package hr.algebra.iis.service;

import hr.algebra.iis.model.Application;
import hr.algebra.iis.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OktaService {

    @Value("${app.use-public-api}")
    private boolean usePublicApi;

    @Value("${app.okta.domain}")
    private String oktaDomain;

    @Value("${app.okta.token}")
    private String oktaToken;

    private final ApplicationRepository applicationRepository;

    public boolean isPublicApiMode() {
        return usePublicApi;
    }

    public Object getApplications() {
        if (usePublicApi) {
            Object response = exchange("/api/v1/apps", HttpMethod.GET, null, Object.class);
            if (response instanceof List<?> apps) {
                return apps.stream().map(this::simplifyApplication).toList();
            }
            return response;
        }
        return applicationRepository.findAll();
    }

    public Object getApplication(String id) {
        return simplifyApplication(exchange("/api/v1/apps/" + id, HttpMethod.GET, null, Object.class));
    }

    public Object createApplication(Application application) {
        Map<String, Object> payload = bookmarkPayload(application);
        Object created = exchange("/api/v1/apps", HttpMethod.POST, payload, Object.class);
        return simplifyApplication(created);
    }

    public Object updateApplication(String id, Application application) {
        Object current = exchange("/api/v1/apps/" + id, HttpMethod.GET, null, Object.class);
        Map<String, Object> payload = bookmarkPayload(application);

        if (current instanceof Map<?, ?> currentMap) {
            Object currentName = currentMap.get("name");
            Object currentSignOnMode = currentMap.get("signOnMode");
            if (currentName instanceof String name && !name.isBlank()) {
                payload.put("name", name);
            }
            if (currentSignOnMode instanceof String signOnMode && !signOnMode.isBlank()) {
                payload.put("signOnMode", signOnMode);
            }
        }

        Object updated = exchange("/api/v1/apps/" + id, HttpMethod.PUT, payload, Object.class);
        syncLifecycle(id, application.getStatus());
        return simplifyApplication(exchange("/api/v1/apps/" + id, HttpMethod.GET, null, Object.class));
    }

    public void deleteApplication(String id) {
        try {
            exchange("/api/v1/apps/" + id + "/lifecycle/deactivate", HttpMethod.POST, Map.of(), Object.class);
        } catch (RuntimeException ignored) {
        }
        exchange("/api/v1/apps/" + id, HttpMethod.DELETE, null, Object.class);
    }

    private Map<String, Object> bookmarkPayload(Application application) {
        String label = firstPresent(application.getLabel(), application.getName(), "Interoperability Application");
        String url = asUrl(application.getExternalId());

        return new LinkedHashMap<>(Map.of(
                "name", "bookmark",
                "label", label,
                "signOnMode", "BOOKMARK",
                "settings", Map.of(
                        "app", Map.of(
                                "requestIntegration", false,
                                "url", url
                        )
                )
        ));
    }

    private String asUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://example.com";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private void syncLifecycle(String id, String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        try {
            if ("ACTIVE".equals(normalized)) {
                exchange("/api/v1/apps/" + id + "/lifecycle/activate", HttpMethod.POST, Map.of(), Object.class);
            } else if ("INACTIVE".equals(normalized)) {
                exchange("/api/v1/apps/" + id + "/lifecycle/deactivate", HttpMethod.POST, Map.of(), Object.class);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private Map<String, Object> simplifyApplication(Object raw) {
        if (!(raw instanceof Map<?, ?> app)) {
            return Map.of();
        }

        String id = stringValue(app.get("id"));
        String name = stringValue(app.get("name"));
        String label = stringValue(app.get("label"));
        String status = stringValue(app.get("status"));
        String signOnMode = stringValue(app.get("signOnMode"));
        String externalId = id;

        Object settingsObj = app.get("settings");
        if (settingsObj instanceof Map<?, ?> settings) {
            Object appObj = settings.get("app");
            if (appObj instanceof Map<?, ?> appSettings) {
                String url = stringValue(appSettings.get("url"));
                if (!url.isBlank()) {
                    externalId = url;
                }
            }
        }

        Map<String, Object> simplified = new LinkedHashMap<>();
        simplified.put("id", id);
        simplified.put("externalId", externalId);
        simplified.put("name", name);
        simplified.put("label", label);
        simplified.put("status", status);
        simplified.put("signOnMode", signOnMode);
        return simplified;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Object exchange(String path, HttpMethod method, Object body, Class<?> responseType) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SSWS " + oktaApiToken());
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<?> response = restTemplate.exchange(
                    baseUrl() + path,
                    method,
                    entity,
                    responseType
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("Okta API error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Neuspjesan poziv prema Okta API-ju: " + e.getMessage(), e);
        }
    }

    private String baseUrl() {
        String value = oktaDomain();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }
        return "https://" + value + ".okta.com";
    }

    private String oktaDomain() {
        return firstPresent(oktaDomain, System.getenv("OKTA_DOMAIN"), readDotenv("OKTA_DOMAIN"));
    }

    private String oktaApiToken() {
        return firstPresent(oktaToken, System.getenv("OKTA_API_TOKEN"), readDotenv("OKTA_API_TOKEN"));
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String readDotenv(String key) {
        Path dotenv = Path.of(".env");
        if (!Files.exists(dotenv)) {
            return "";
        }

        try (Stream<String> lines = Files.lines(dotenv)) {
            Optional<String> match = lines
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> line.startsWith(key + "="))
                    .map(line -> line.substring((key + "=").length()).trim())
                    .findFirst();
            return match.orElse("");
        } catch (IOException e) {
            return "";
        }
    }
}
