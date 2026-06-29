package hr.algebra.iis.okta.weather.dhmz.client;

import hr.algebra.iis.okta.weather.dhmz.config.DhmzWeatherProperties;
import hr.algebra.iis.okta.weather.dhmz.exception.DhmzWeatherException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class DhmzWeatherClient {

    private final DhmzWeatherProperties properties;
    private final HttpClient httpClient;

    public DhmzWeatherClient(
            DhmzWeatherProperties properties,
            @Qualifier("dhmzWeatherHttpClient") HttpClient httpClient
    ) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    public String fetchObservationsXml() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.observationsUrl()))
                .timeout(properties.timeout())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new DhmzWeatherException(
                        "DHMZ observations endpoint returned status %d".formatted(response.statusCode()),
                        null
                );
            }
            return response.body();
        } catch (IOException exception) {
            throw new DhmzWeatherException("Unable to fetch DHMZ observations XML", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DhmzWeatherException("DHMZ observations request was interrupted", exception);
        }
    }
}
