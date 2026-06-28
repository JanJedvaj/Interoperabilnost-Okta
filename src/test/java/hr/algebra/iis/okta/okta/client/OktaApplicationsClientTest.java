package hr.algebra.iis.okta.okta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hr.algebra.iis.okta.okta.config.OktaProperties;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import hr.algebra.iis.okta.okta.exception.OktaApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OktaApplicationsClientTest {

    private static final String API_TOKEN = "test-okta-token";

    private HttpServer server;
    private OktaApplicationsClient client;
    private final List<CapturedRequest> capturedRequests = new ArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/apps", this::handleApplications);
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        client = new OktaApplicationsClient(
                new OktaProperties(baseUrl, API_TOKEN, "/api/v1/apps", Duration.ofSeconds(5)),
                HttpClient.newHttpClient(),
                objectMapper,
                new OktaApplicationMapper()
        );
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void listsApplicationsAcrossPaginatedOktaResponses() {
        List<OktaApplicationResponse> applications = client.listApplications(1);

        assertEquals(2, applications.size());
        assertEquals("app-page-1", applications.get(0).id());
        assertEquals("CRM Portal", applications.get(0).label());
        assertEquals("app-page-2", applications.get(1).id());
        assertEquals("Partner SSO", applications.get(1).label());

        assertEquals(2, capturedRequests.size());
        assertEquals("/api/v1/apps?limit=1", capturedRequests.get(0).pathAndQuery());
        assertEquals("/api/v1/apps?after=page-2", capturedRequests.get(1).pathAndQuery());
        assertTrue(capturedRequests.stream().allMatch(request -> request.authorization().equals("SSWS " + API_TOKEN)));
    }

    @Test
    void retrievesApplicationById() {
        OktaApplicationResponse response = client.getApplication("single-app");

        assertEquals("single-app", response.id());
        assertEquals("Single Application", response.label());
        assertEquals("OPENID_CONNECT", response.signOnMode());
    }

    @Test
    void createsApplicationWithJsonPayload() {
        OktaApplicationResponse response = client.createApplication(sampleRequest("Created App"));

        assertEquals("created-app", response.id());
        assertEquals("Created App", response.label());
        CapturedRequest request = capturedRequests.get(0);
        assertEquals("POST", request.method());
        assertTrue(request.body().contains("\"label\":\"Created App\""));
        assertTrue(request.body().contains("\"settings\""));
    }

    @Test
    void replacesApplicationWithJsonPayload() {
        OktaApplicationResponse response = client.replaceApplication("replace-app", sampleRequest("Replaced App"));

        assertEquals("replace-app", response.id());
        assertEquals("Replaced App", response.label());
        CapturedRequest request = capturedRequests.get(0);
        assertEquals("PUT", request.method());
        assertTrue(request.body().contains("\"label\":\"Replaced App\""));
    }

    @Test
    void deletesApplication() {
        client.deleteApplication("delete-app");

        assertEquals("DELETE", capturedRequests.get(0).method());
        assertEquals("/api/v1/apps/delete-app", capturedRequests.get(0).pathAndQuery());
    }

    @Test
    void throwsApiExceptionForUnauthorizedOktaResponse() {
        OktaApiException exception = assertThrows(
                OktaApiException.class,
                () -> client.getApplication("unauthorized")
        );

        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void throwsApiExceptionForRateLimitedOktaResponse() {
        OktaApiException exception = assertThrows(
                OktaApiException.class,
                () -> client.getApplication("rate-limited")
        );

        assertEquals(429, exception.getStatusCode());
    }

    private void handleApplications(HttpExchange exchange) {
        try (exchange) {
            CapturedRequest capturedRequest = CapturedRequest.from(exchange);
            capturedRequests.add(capturedRequest);

            if (!("SSWS " + API_TOKEN).equals(capturedRequest.authorization())) {
                send(exchange, 401, "{\"errorSummary\":\"Invalid token\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String method = exchange.getRequestMethod();

            if ("/api/v1/apps".equals(path) && "GET".equals(method) && "limit=1".equals(query)) {
                String next = "http://localhost:%d/api/v1/apps?after=page-2".formatted(server.getAddress().getPort());
                exchange.getResponseHeaders().add("Link", "<%s>; rel=\"next\"".formatted(next));
                send(exchange, 200, "[" + applicationJson("app-page-1", "CRM Portal", "OPENID_CONNECT") + "]");
                return;
            }

            if ("/api/v1/apps".equals(path) && "GET".equals(method) && "after=page-2".equals(query)) {
                send(exchange, 200, "[" + applicationJson("app-page-2", "Partner SSO", "SAML_2_0") + "]");
                return;
            }

            if ("/api/v1/apps/single-app".equals(path) && "GET".equals(method)) {
                send(exchange, 200, applicationJson("single-app", "Single Application", "OPENID_CONNECT"));
                return;
            }

            if ("/api/v1/apps".equals(path) && "POST".equals(method)) {
                send(exchange, 200, applicationJson("created-app", "Created App", "OPENID_CONNECT"));
                return;
            }

            if ("/api/v1/apps/replace-app".equals(path) && "PUT".equals(method)) {
                send(exchange, 200, applicationJson("replace-app", "Replaced App", "OPENID_CONNECT"));
                return;
            }

            if ("/api/v1/apps/delete-app".equals(path) && "DELETE".equals(method)) {
                send(exchange, 204, "");
                return;
            }

            if ("/api/v1/apps/unauthorized".equals(path)) {
                send(exchange, 401, "{\"errorSummary\":\"Invalid token\"}");
                return;
            }

            if ("/api/v1/apps/rate-limited".equals(path)) {
                send(exchange, 429, "{\"errorSummary\":\"Too many requests\"}");
                return;
            }

            send(exchange, 404, "{\"errorSummary\":\"Not found\"}");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static OktaApplicationRequest sampleRequest(String label) {
        ObjectNode settings = JsonNodeFactory.instance.objectNode();
        ObjectNode appSettings = settings.putObject("app");
        appSettings.put("loginUrl", "https://example.com/login");

        return new OktaApplicationRequest("bookmark_app", label, "BOOKMARK", settings);
    }

    private static String applicationJson(String id, String label, String signOnMode) {
        return """
                {
                  "id": "%s",
                  "name": "oidc_client",
                  "label": "%s",
                  "status": "ACTIVE",
                  "signOnMode": "%s",
                  "created": "2026-06-24T12:00:00.000Z",
                  "lastUpdated": "2026-06-24T12:30:00.000Z"
                }
                """.formatted(id, label, signOnMode);
    }

    private static void send(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        if (statusCode == 204) {
            exchange.sendResponseHeaders(statusCode, -1);
            return;
        }

        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private record CapturedRequest(String method, String pathAndQuery, String authorization, String body) {

        static CapturedRequest from(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String pathAndQuery = uri.getRawQuery() == null
                    ? uri.getRawPath()
                    : uri.getRawPath() + "?" + uri.getRawQuery();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            return new CapturedRequest(
                    exchange.getRequestMethod(),
                    pathAndQuery,
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    body
            );
        }
    }
}
