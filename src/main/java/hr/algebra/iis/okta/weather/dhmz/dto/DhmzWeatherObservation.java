package hr.algebra.iis.okta.weather.dhmz.dto;

public record DhmzWeatherObservation(
        String city,
        String temperature,
        String humidity,
        String pressure,
        String pressureTendency,
        String windDirection,
        String windSpeed,
        String weather
) {
}
