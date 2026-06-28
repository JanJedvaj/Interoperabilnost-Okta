package hr.algebra.iis.okta.soap.config;

import hr.algebra.iis.okta.soap.application.ApplicationSoapEndpoint;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapEndpointConfig {

    @Bean
    Endpoint applicationSearchEndpoint(Bus bus, ApplicationSoapEndpoint applicationSoapEndpoint) {
        EndpointImpl endpoint = new EndpointImpl(bus, applicationSoapEndpoint);
        endpoint.publish("/applications");
        return endpoint;
    }
}
