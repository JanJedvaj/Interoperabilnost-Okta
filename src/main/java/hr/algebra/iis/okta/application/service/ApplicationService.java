package hr.algebra.iis.okta.application.service;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.model.ApplicationEntity;
import hr.algebra.iis.okta.application.repository.ApplicationRepository;
import hr.algebra.iis.okta.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll()
                .stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return ApplicationResponse.fromEntity(findEntityById(id));
    }

    public ApplicationResponse create(ApplicationRequest request) {
        ApplicationEntity entity = new ApplicationEntity(
                normalizeOptional(request.externalId()),
                request.name(),
                request.label(),
                request.status(),
                request.signOnMode()
        );

        return ApplicationResponse.fromEntity(applicationRepository.save(entity));
    }

    public ApplicationResponse update(Long id, ApplicationRequest request) {
        ApplicationEntity entity = findEntityById(id);
        entity.setExternalId(normalizeOptional(request.externalId()));
        entity.setName(request.name());
        entity.setLabel(request.label());
        entity.setStatus(request.status());
        entity.setSignOnMode(request.signOnMode());

        return ApplicationResponse.fromEntity(applicationRepository.save(entity));
    }

    public void delete(Long id) {
        ApplicationEntity entity = findEntityById(id);
        applicationRepository.delete(entity);
    }

    private ApplicationEntity findEntityById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with id %d was not found".formatted(id)));
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
