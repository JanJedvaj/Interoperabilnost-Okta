package hr.algebra.iis.okta.application.sync;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.service.ApplicationService;
import hr.algebra.iis.okta.okta.client.OktaApplicationsApi;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationSyncService {

    private final OktaApplicationsApi oktaApplicationsApi;
    private final ApplicationService applicationService;

    public ApplicationSyncService(OktaApplicationsApi oktaApplicationsApi, ApplicationService applicationService) {
        this.oktaApplicationsApi = oktaApplicationsApi;
        this.applicationService = applicationService;
    }

    public ApplicationSyncResult syncFromOkta(Integer limit) {
        List<OktaApplicationResponse> oktaApplications = oktaApplicationsApi.listApplications(limit);

        int saved = 0;
        for (OktaApplicationResponse oktaApplication : oktaApplications) {
            applicationService.upsertExternal(toApplicationRequest(oktaApplication));
            saved++;
        }

        return new ApplicationSyncResult(oktaApplications.size(), saved);
    }

    private static ApplicationRequest toApplicationRequest(OktaApplicationResponse response) {
        return new ApplicationRequest(
                response.id(),
                fallback(response.name(), "unknown"),
                fallback(response.label(), response.name()),
                fallback(response.status(), "UNKNOWN"),
                fallback(response.signOnMode(), "UNKNOWN")
        );
    }

    private static String fallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
