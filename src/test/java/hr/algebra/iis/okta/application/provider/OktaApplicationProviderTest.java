package hr.algebra.iis.okta.application.provider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.okta.client.OktaApplicationsApi;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OktaApplicationProviderTest {

    @Test
    void mapsOktaApplicationsToSharedApplicationResponses() {
        FakeOktaApplicationsApi oktaApi = new FakeOktaApplicationsApi(List.of(oktaApplication("okta-app-1", "CRM")));
        OktaApplicationProvider provider = new OktaApplicationProvider(oktaApi);

        ApplicationResponse response = provider.findById("okta-app-1");

        assertNull(response.id());
        assertEquals("okta-app-1", response.externalId());
        assertEquals("CRM", response.label());
        assertEquals("okta-app-1", response.resourceId());
    }

    @Test
    void convertsSharedApplicationRequestWhenCreatingInOkta() {
        FakeOktaApplicationsApi oktaApi = new FakeOktaApplicationsApi(List.of());
        OktaApplicationProvider provider = new OktaApplicationProvider(oktaApi);

        provider.create(new ApplicationRequest("external-id-is-local-only", "bookmark_app", "Bookmark", "ACTIVE", "BOOKMARK"));

        assertEquals("bookmark_app", oktaApi.lastCreateRequest.name());
        assertEquals("Bookmark", oktaApi.lastCreateRequest.label());
        assertEquals("BOOKMARK", oktaApi.lastCreateRequest.signOnMode());
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

    private static final class FakeOktaApplicationsApi implements OktaApplicationsApi {

        private final List<OktaApplicationResponse> applications;
        private OktaApplicationRequest lastCreateRequest;

        private FakeOktaApplicationsApi(List<OktaApplicationResponse> applications) {
            this.applications = new ArrayList<>(applications);
        }

        @Override
        public List<OktaApplicationResponse> listApplications(Integer limit) {
            return applications;
        }

        @Override
        public OktaApplicationResponse getApplication(String id) {
            return applications.stream()
                    .filter(application -> application.id().equals(id))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public OktaApplicationResponse createApplication(OktaApplicationRequest request) {
            lastCreateRequest = request;
            return oktaApplication("created-okta-app", request.label());
        }

        @Override
        public OktaApplicationResponse replaceApplication(String id, OktaApplicationRequest request) {
            return oktaApplication(id, request.label());
        }

        @Override
        public void deleteApplication(String id) {
        }
    }
}
