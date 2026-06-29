package hr.algebra.iis.okta.weather.dhmz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.dhmz")
public record DhmzWeatherProperties(
        String observationsUrl,
        Duration timeout
) {

    public DhmzWeatherProperties {
        observationsUrl = observationsUrl == null || observationsUrl.isBlank()
                ? "https://vrijeme.hr/hrvatska_n.xml"
                : observationsUrl;
        timeout = timeout == null ? Duration.ofSeconds(20) : timeout;
    }
}
