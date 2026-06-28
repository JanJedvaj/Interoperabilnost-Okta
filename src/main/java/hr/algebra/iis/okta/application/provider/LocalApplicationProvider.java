package hr.algebra.iis.okta.application.provider;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.service.ApplicationService;
import hr.algebra.iis.okta.common.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.api-mode", havingValue = "LOCAL", matchIfMissing = true)
public class LocalApplicationProvider implements ApplicationProvider {

    private final ApplicationService applicationService;

    public LocalApplicationProvider(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public List<ApplicationResponse> findAll() {
        return applicationService.findAll();
    }

    @Override
    public ApplicationResponse findById(String id) {
        return applicationService.findById(parseLocalId(id));
    }

    @Override
    public ApplicationResponse create(ApplicationRequest request) {
        return applicationService.create(request);
    }

    @Override
    public ApplicationResponse update(String id, ApplicationRequest request) {
        return applicationService.update(parseLocalId(id), request);
    }

    @Override
    public void delete(String id) {
        applicationService.delete(parseLocalId(id));
    }

    private static Long parseLocalId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new ResourceNotFoundException("Application with id %s was not found".formatted(id));
        }
    }
}
