package hr.algebra.iis.okta.application.sync;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import hr.algebra.iis.okta.application.repository.ApplicationRepository;
import hr.algebra.iis.okta.application.service.ApplicationService;
import hr.algebra.iis.okta.okta.client.OktaApplicationsApi;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:application_sync_service_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApplicationSyncServiceTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
    }

    @Test
    void syncsOktaApplicationsIntoLocalDatabaseByExternalId() {
        ApplicationSyncService syncService = new ApplicationSyncService(
                new FakeOktaApplicationsApi(List.of(
                        oktaApplication("okta-app-1", "CRM Portal"),
                        oktaApplication("okta-app-2", "Partner SSO")
                )),
                applicationService
        );

        ApplicationSyncResult firstSync = syncService.syncFromOkta(null);
        ApplicationSyncResult secondSync = syncService.syncFromOkta(null);

        assertEquals(2, firstSync.receivedFromOkta());
        assertEquals(2, firstSync.savedLocally());
        assertEquals(2, secondSync.savedLocally());
        assertEquals(2, applicationRepository.count());
        assertEquals("CRM Portal", applicationRepository.findByExternalId("okta-app-1").orElseThrow().getLabel());
    }

    private static OktaApplicationResponse oktaApplication(String id, String label) {
        return new OktaApplicationResponse(
                id,
                "oidc_client",
                label,
                "ACTIVE",
                "OPENID_CONNECT",
                OffsetDateTime.parse("2026-06-24T12:00:00Z"),
                OffsetDateTime.parse("2026-06-24T12:30:00Z"),
                JsonNodeFactory.instance.objectNode()
        );
    }

    private record FakeOktaApplicationsApi(List<OktaApplicationResponse> applications) implements OktaApplicationsApi {

        @Override
        public List<OktaApplicationResponse> listApplications(Integer limit) {
            return applications;
        }

        @Override
        public OktaApplicationResponse getApplication(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OktaApplicationResponse createApplication(OktaApplicationRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OktaApplicationResponse replaceApplication(String id, OktaApplicationRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteApplication(String id) {
            throw new UnsupportedOperationException();
        }
    }
}
