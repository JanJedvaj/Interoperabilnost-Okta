package hr.algebra.iis.okta.weather.dhmz.service;

import hr.algebra.iis.okta.weather.dhmz.client.DhmzWeatherClient;
import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import hr.algebra.iis.okta.weather.dhmz.parser.DhmzWeatherParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DhmzWeatherService {

    private final DhmzWeatherClient client;
    private final DhmzWeatherParser parser;

    public DhmzWeatherService(DhmzWeatherClient client, DhmzWeatherParser parser) {
        this.client = client;
        this.parser = parser;
    }

    public List<DhmzWeatherObservation> searchCities(String query) {
        String normalizedQuery = normalize(query);
        return parser.parse(client.fetchObservationsXml())
                .stream()
                .filter(observation -> normalize(observation.city()).contains(normalizedQuery))
                .toList();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
