package hr.algebra.iis.okta.application.provider;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;

import java.util.List;

public interface ApplicationProvider {

    List<ApplicationResponse> findAll();

    ApplicationResponse findById(String id);

    ApplicationResponse create(ApplicationRequest request);

    ApplicationResponse update(String id, ApplicationRequest request);

    void delete(String id);
}
