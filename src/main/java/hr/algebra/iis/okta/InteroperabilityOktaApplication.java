package hr.algebra.iis.okta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InteroperabilityOktaApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteroperabilityOktaApplication.class, args);
    }
}
