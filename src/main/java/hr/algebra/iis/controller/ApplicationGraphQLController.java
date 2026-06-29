package hr.algebra.iis.controller;

import hr.algebra.iis.model.Application;
import hr.algebra.iis.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ApplicationGraphQLController {

    private final ApplicationService applicationService;

    @QueryMapping
    public List<Application> applications() {
        return applicationService.findAll();
    }

    @QueryMapping
    public Application application(@Argument Long id) {
        return applicationService.findById(id);
    }

    @PreAuthorize("hasRole('FULL_ACCESS')")
    @MutationMapping
    public Application createApplication(@Argument String externalId, @Argument String name,
                                         @Argument String label, @Argument String status,
                                         @Argument String signOnMode) {
        return applicationService.create(externalId, name, label, status, signOnMode);
    }

    @PreAuthorize("hasRole('FULL_ACCESS')")
    @MutationMapping
    public Application updateApplication(@Argument Long id, @Argument String externalId, @Argument String name,
                                         @Argument String label, @Argument String status,
                                         @Argument String signOnMode) {
        return applicationService.update(id, Application.builder()
                .externalId(externalId)
                .name(name)
                .label(label)
                .status(status)
                .signOnMode(signOnMode)
                .build());
    }

    @PreAuthorize("hasRole('FULL_ACCESS')")
    @MutationMapping
    public Boolean deleteApplication(@Argument Long id) {
        applicationService.deleteById(id);
        return true;
    }
}
