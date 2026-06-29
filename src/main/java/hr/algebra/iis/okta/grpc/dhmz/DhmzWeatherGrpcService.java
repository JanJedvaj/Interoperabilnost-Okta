package hr.algebra.iis.okta.grpc.dhmz;

import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class DhmzWeatherGrpcService extends DhmzWeatherServiceGrpc.DhmzWeatherServiceImplBase {

    private final hr.algebra.iis.okta.weather.dhmz.service.DhmzWeatherService dhmzWeatherService;

    public DhmzWeatherGrpcService(hr.algebra.iis.okta.weather.dhmz.service.DhmzWeatherService dhmzWeatherService) {
        this.dhmzWeatherService = dhmzWeatherService;
    }

    @Override
    public void searchCities(SearchCitiesRequest request, StreamObserver<SearchCitiesResponse> responseObserver) {
        SearchCitiesResponse.Builder responseBuilder = SearchCitiesResponse.newBuilder();

        dhmzWeatherService.searchCities(request.getQuery())
                .stream()
                .map(DhmzWeatherGrpcService::toGrpcCity)
                .forEach(responseBuilder::addCities);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private static WeatherCity toGrpcCity(DhmzWeatherObservation observation) {
        return WeatherCity.newBuilder()
                .setCity(safe(observation.city()))
                .setTemperature(safe(observation.temperature()))
                .setHumidity(safe(observation.humidity()))
                .setPressure(safe(observation.pressure()))
                .setPressureTendency(safe(observation.pressureTendency()))
                .setWindDirection(safe(observation.windDirection()))
                .setWindSpeed(safe(observation.windSpeed()))
                .setWeather(safe(observation.weather()))
                .build();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
