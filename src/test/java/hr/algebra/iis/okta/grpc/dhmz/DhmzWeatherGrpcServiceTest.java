package hr.algebra.iis.okta.grpc.dhmz;

import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DhmzWeatherGrpcServiceTest {

    @Test
    void mapsWeatherServiceResultsToGrpcResponse() {
        hr.algebra.iis.okta.weather.dhmz.service.DhmzWeatherService weatherService =
                mock(hr.algebra.iis.okta.weather.dhmz.service.DhmzWeatherService.class);
        when(weatherService.searchCities("zag")).thenReturn(List.of(new DhmzWeatherObservation(
                "Zagreb-Maksimir",
                "21.4",
                "65",
                "1015.2",
                "+",
                "NE",
                "2.1",
                "Vedro"
        )));

        CapturingObserver observer = new CapturingObserver();

        new DhmzWeatherGrpcService(weatherService).searchCities(
                SearchCitiesRequest.newBuilder().setQuery("zag").build(),
                observer
        );

        assertTrue(observer.completed);
        assertEquals(1, observer.response.getCitiesCount());
        assertEquals("Zagreb-Maksimir", observer.response.getCities(0).getCity());
        assertEquals("21.4", observer.response.getCities(0).getTemperature());
    }

    private static final class CapturingObserver implements StreamObserver<SearchCitiesResponse> {

        private SearchCitiesResponse response;
        private boolean completed;

        @Override
        public void onNext(SearchCitiesResponse value) {
            response = value;
        }

        @Override
        public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }
}
