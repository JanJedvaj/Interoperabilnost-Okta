package hr.algebra.iis.okta.okta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.iis.okta.okta.config.OktaProperties;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import hr.algebra.iis.okta.okta.exception.OktaApiException;
import hr.algebra.iis.okta.okta.exception.OktaClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OktaApplicationsClient implements OktaApplicationsApi {

    private static final String ACCEPT = "Accept";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String LINK = "Link";

    private final OktaProperties oktaProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OktaApplicationMapper oktaApplicationMapper;

    public OktaApplicationsClient(
            OktaProperties oktaProperties,
            HttpClient httpClient,
            ObjectMapper objectMapper,
            OktaApplicationMapper oktaApplicationMapper
    ) {
        this.oktaProperties = oktaProperties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.oktaApplicationMapper = oktaApplicationMapper;
    }

    @Override
    public List<OktaApplicationResponse> listApplications(Integer limit) {
        List<OktaApplicationResponse> applications = new ArrayList<>();
        URI nextUri = UriComponentsBuilder
                .fromUriString(applicationsUrl())
                .queryParamIfPresent("limit", Optional.ofNullable(limit))
                .build()
                .toUri();

        while (nextUri != null) {
            HttpResponse<String> response = send(requestBuilder(nextUri).GET().build());
            ensureSuccess(response, 200);

            try {
                JsonNode root = objectMapper.readTree(response.body());
                if (!root.isArray()) {
                    throw new OktaClientException("Okta list applications response was not a JSON array", null);
                }

                for (JsonNode item : root) {
                    applications.add(oktaApplicationMapper.toResponse(item));
                }
            } catch (JsonProcessingException exception) {
                throw new OktaClientException("Unable to parse Okta list applications response", exception);
            }

            nextUri = extractNextUri(response).orElse(null);
        }

        return applications;
    }

    @Override
    public OktaApplicationResponse getApplication(String id) {
        HttpResponse<String> response = send(requestBuilder(applicationUrl(id)).GET().build());
        ensureSuccess(response, 200);
        return parseApplication(response.body());
    }

    @Override
    public OktaApplicationResponse createApplication(OktaApplicationRequest request) {
        HttpResponse<String> response = send(requestBuilder(applicationsUrl())
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(request)))
                .build());
        ensureSuccess(response, 200);
        return parseApplication(response.body());
    }

    @Override
    public OktaApplicationResponse replaceApplication(String id, OktaApplicationRequest request) {
        HttpResponse<String> response = send(requestBuilder(applicationUrl(id))
                .PUT(HttpRequest.BodyPublishers.ofString(writeJson(request)))
                .build());
        ensureSuccess(response, 200);
        return parseApplication(response.body());
    }

    @Override
    public void deleteApplication(String id) {
        HttpResponse<String> response = send(requestBuilder(applicationUrl(id)).DELETE().build());
        ensureSuccess(response, 204);
    }

    private HttpRequest.Builder requestBuilder(String url) {
        return requestBuilder(URI.create(url));
    }

    private HttpRequest.Builder requestBuilder(URI uri) {
        return HttpRequest.newBuilder(uri)
                .timeout(oktaProperties.timeout())
                .header(ACCEPT, APPLICATION_JSON)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, "SSWS " + oktaProperties.requiredApiToken());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new OktaClientException("Unable to reach Okta Applications API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OktaClientException("Okta Applications API request was interrupted", exception);
        }
    }

    private void ensureSuccess(HttpResponse<String> response, int expectedStatus) {
        if (response.statusCode() != expectedStatus) {
            throw new OktaApiException(response.statusCode(), response.body());
        }
    }

    private OktaApplicationResponse parseApplication(String body) {
        try {
            return oktaApplicationMapper.toResponse(objectMapper.readTree(body));
        } catch (JsonProcessingException exception) {
            throw new OktaClientException("Unable to parse Okta application response", exception);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new OktaClientException("Unable to serialize Okta application request", exception);
        }
    }

    private Optional<URI> extractNextUri(HttpResponse<String> response) {
        return response.headers()
                .firstValue(LINK)
                .flatMap(this::extractNextLink);
    }

    private Optional<URI> extractNextLink(String linkHeader) {
        for (String linkPart : linkHeader.split(",")) {
            String trimmed = linkPart.trim();
            if (trimmed.contains("rel=\"next\"")) {
                int start = trimmed.indexOf('<');
                int end = trimmed.indexOf('>');
                if (start >= 0 && end > start) {
                    return Optional.of(URI.create(trimmed.substring(start + 1, end)));
                }
            }
        }

        return Optional.empty();
    }

    private String applicationsUrl() {
        return oktaProperties.normalizedBaseUrl() + oktaProperties.applicationsPath();
    }

    private String applicationUrl(String id) {
        return applicationsUrl() + "/" + id;
    }
}
