package hr.algebra.iis.okta.weather.dhmz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class DhmzWeatherHttpClientConfig {

    @Bean
    HttpClient dhmzWeatherHttpClient(DhmzWeatherProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
    }
}
