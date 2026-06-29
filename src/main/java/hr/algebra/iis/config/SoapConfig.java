package hr.algebra.iis.config;

import hr.algebra.iis.soap.ApplicationSoapService;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapConfig {

    @Bean
    public Endpoint applicationEndpoint(Bus bus, ApplicationSoapService applicationSoapService) {
        EndpointImpl endpoint = new EndpointImpl(bus, applicationSoapService);
        endpoint.publish("/applications");
        return endpoint;
    }
}
