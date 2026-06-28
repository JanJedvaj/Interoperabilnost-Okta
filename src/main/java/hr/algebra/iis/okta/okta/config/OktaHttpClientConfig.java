package hr.algebra.iis.okta.okta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class OktaHttpClientConfig {

    @Bean
    HttpClient oktaHttpClient(OktaProperties oktaProperties) {
        return HttpClient.newBuilder()
                .connectTimeout(oktaProperties.timeout())
                .build();
    }
}
