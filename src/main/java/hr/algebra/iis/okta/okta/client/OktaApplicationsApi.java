package hr.algebra.iis.okta.okta.client;

import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;

import java.util.List;

public interface OktaApplicationsApi {

    List<OktaApplicationResponse> listApplications(Integer limit);

    OktaApplicationResponse getApplication(String id);

    OktaApplicationResponse createApplication(OktaApplicationRequest request);

    OktaApplicationResponse replaceApplication(String id, OktaApplicationRequest request);

    void deleteApplication(String id);
}
