package hr.algebra.iis.okta.weather.dhmz.service;

import hr.algebra.iis.okta.weather.dhmz.client.DhmzWeatherClient;
import hr.algebra.iis.okta.weather.dhmz.config.DhmzWeatherProperties;
import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import hr.algebra.iis.okta.weather.dhmz.parser.DhmzWeatherParser;
import hr.algebra.iis.okta.weather.dhmz.parser.DhmzWeatherParserTest;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DhmzWeatherServiceTest {

    @Test
    void searchesCitiesByCaseInsensitivePartialName() {
        DhmzWeatherService service = new DhmzWeatherService(
                new StaticDhmzWeatherClient(DhmzWeatherParserTest.sampleDhmzXml()),
                new DhmzWeatherParser()
        );

        List<DhmzWeatherObservation> observations = service.searchCities("zag");

        assertEquals(1, observations.size());
        assertEquals("Zagreb-Maksimir", observations.getFirst().city());
    }

    private static final class StaticDhmzWeatherClient extends DhmzWeatherClient {

        private final String xml;

        private StaticDhmzWeatherClient(String xml) {
            super(
                    new DhmzWeatherProperties("https://example.com/hrvatska_n.xml", Duration.ofSeconds(1)),
                    HttpClient.newHttpClient()
            );
            this.xml = xml;
        }

        @Override
        public String fetchObservationsXml() {
            return xml;
        }
    }
}
