package hr.algebra.iis.okta.soap.application;

import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchItem;
import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchResult;
import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationSoapEndpointImplTest {

    @Test
    void mapsXmlSearchResultToSoapResponse() {
        ApplicationXmlSearchService searchService = mock(ApplicationXmlSearchService.class);
        when(searchService.search("CRM")).thenReturn(new ApplicationXmlSearchResult(
                "CRM",
                "<applications count=\"1\"/>",
                true,
                List.of(),
                List.of(new ApplicationXmlSearchItem(
                        "1",
                        1L,
                        "okta-app-1",
                        "oidc_client",
                        "CRM Portal",
                        "ACTIVE",
                        "OPENID_CONNECT",
                        "2026-06-24T12:00:00Z",
                        "2026-06-24T12:30:00Z"
                ))
        ));

        ApplicationSoapSearchResponse response = new ApplicationSoapEndpointImpl(searchService).searchApplications("CRM");

        assertEquals("CRM", response.getSearchTerm());
        assertEquals(1, response.getApplications().size());
        assertEquals("CRM Portal", response.getApplications().getFirst().getLabel());
    }
}
