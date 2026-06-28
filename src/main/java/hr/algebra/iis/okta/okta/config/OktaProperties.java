package hr.algebra.iis.okta.okta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.okta")
public record OktaProperties(
        String domain,
        String apiToken,
        String applicationsPath,
        Duration timeout
) {

    public OktaProperties {
        applicationsPath = applicationsPath == null || applicationsPath.isBlank()
                ? "/api/v1/apps"
                : applicationsPath;
        timeout = timeout == null ? Duration.ofSeconds(20) : timeout;
    }

    public String normalizedBaseUrl() {
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Okta domain is not configured. Set OKTA_DOMAIN or app.okta.domain.");
        }

        String trimmed = domain.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return removeTrailingSlash(trimmed);
        }

        return "https://" + removeTrailingSlash(trimmed) + ".okta.com";
    }

    public String requiredApiToken() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("Okta API token is not configured. Set OKTA_API_TOKEN or app.okta.api-token.");
        }

        return apiToken.trim();
    }

    private static String removeTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
