package hr.algebra.iis.okta.application.provider;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.okta.client.OktaApplicationsApi;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.api-mode", havingValue = "OKTA")
public class OktaApplicationProvider implements ApplicationProvider {

    private final OktaApplicationsApi oktaApplicationsApi;

    public OktaApplicationProvider(OktaApplicationsApi oktaApplicationsApi) {
        this.oktaApplicationsApi = oktaApplicationsApi;
    }

    @Override
    public List<ApplicationResponse> findAll() {
        return oktaApplicationsApi.listApplications(null)
                .stream()
                .map(OktaApplicationProvider::toApplicationResponse)
                .toList();
    }

    @Override
    public ApplicationResponse findById(String id) {
        return toApplicationResponse(oktaApplicationsApi.getApplication(id));
    }

    @Override
    public ApplicationResponse create(ApplicationRequest request) {
        return toApplicationResponse(oktaApplicationsApi.createApplication(toOktaRequest(request)));
    }

    @Override
    public ApplicationResponse update(String id, ApplicationRequest request) {
        return toApplicationResponse(oktaApplicationsApi.replaceApplication(id, toOktaRequest(request)));
    }

    @Override
    public void delete(String id) {
        oktaApplicationsApi.deleteApplication(id);
    }

    private static ApplicationResponse toApplicationResponse(OktaApplicationResponse response) {
        return new ApplicationResponse(
                null,
                response.id(),
                response.name(),
                response.label(),
                response.status(),
                response.signOnMode(),
                response.created(),
                response.lastUpdated()
        );
    }

    private static OktaApplicationRequest toOktaRequest(ApplicationRequest request) {
        return OktaApplicationRequest.fromApplicationRequest(request);
    }
}
