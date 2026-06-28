package hr.algebra.iis.okta.application.xml;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.provider.ApplicationProvider;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationXmlSearchServiceTest {

    private final ApplicationXmlValidator validator = createValidator();
    private final ApplicationXmlDocumentService documentService = new ApplicationXmlDocumentService(validator);

    @Test
    void generatesValidXmlSnapshot() {
        ApplicationXmlDocument snapshot = documentService.createSnapshot(sampleApplications());

        assertTrue(snapshot.validationResult().valid());
        assertTrue(snapshot.xml().contains("<applications count=\"2\">"));
        assertTrue(snapshot.xml().contains("<label>CRM Portal</label>"));
    }

    @Test
    void searchesApplicationsWithXPathOverGeneratedXml() {
        ApplicationXmlSearchService searchService = new ApplicationXmlSearchService(
                new FakeApplicationProvider(sampleApplications()),
                documentService
        );

        ApplicationXmlSearchResult result = searchService.search("Partner");

        assertTrue(result.xmlValid());
        assertEquals(1, result.applications().size());
        assertEquals("okta-app-2", result.applications().getFirst().externalId());
        assertEquals("Partner SSO", result.applications().getFirst().label());
    }

    @Test
    void returnsNoMatchesWhenSearchTermDoesNotExist() {
        ApplicationXmlSearchService searchService = new ApplicationXmlSearchService(
                new FakeApplicationProvider(sampleApplications()),
                documentService
        );

        ApplicationXmlSearchResult result = searchService.search("missing");

        assertTrue(result.xmlValid());
        assertEquals(0, result.applications().size());
    }

    private static List<ApplicationResponse> sampleApplications() {
        return List.of(
                new ApplicationResponse(
                        1L,
                        "okta-app-1",
                        "oidc_client",
                        "CRM Portal",
                        "ACTIVE",
                        "OPENID_CONNECT",
                        OffsetDateTime.parse("2026-06-24T12:00:00Z"),
                        OffsetDateTime.parse("2026-06-24T12:30:00Z")
                ),
                new ApplicationResponse(
                        2L,
                        "okta-app-2",
                        "saml_app",
                        "Partner SSO",
                        "INACTIVE",
                        "SAML_2_0",
                        OffsetDateTime.parse("2026-06-24T13:00:00Z"),
                        OffsetDateTime.parse("2026-06-24T13:30:00Z")
                )
        );
    }

    private static ApplicationXmlValidator createValidator() {
        try {
            return new ApplicationXmlValidator();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record FakeApplicationProvider(List<ApplicationResponse> applications) implements ApplicationProvider {

        @Override
        public List<ApplicationResponse> findAll() {
            return applications;
        }

        @Override
        public ApplicationResponse findById(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApplicationResponse create(ApplicationRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApplicationResponse update(String id, ApplicationRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(String id) {
            throw new UnsupportedOperationException();
        }
    }
}
