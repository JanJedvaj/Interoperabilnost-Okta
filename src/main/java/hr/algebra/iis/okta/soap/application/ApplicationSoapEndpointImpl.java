package hr.algebra.iis.okta.soap.application;

import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchService;
import jakarta.jws.WebService;
import org.springframework.stereotype.Component;

@Component
@WebService(
        endpointInterface = "hr.algebra.iis.okta.soap.application.ApplicationSoapEndpoint",
        serviceName = "ApplicationSearchService",
        portName = "ApplicationSearchPort",
        targetNamespace = "https://algebra.hr/iis/okta/applications"
)
public class ApplicationSoapEndpointImpl implements ApplicationSoapEndpoint {

    private final ApplicationXmlSearchService applicationXmlSearchService;

    public ApplicationSoapEndpointImpl(ApplicationXmlSearchService applicationXmlSearchService) {
        this.applicationXmlSearchService = applicationXmlSearchService;
    }

    @Override
    public ApplicationSoapSearchResponse searchApplications(String searchTerm) {
        return new ApplicationSoapSearchResponse(applicationXmlSearchService.search(searchTerm));
    }
}
