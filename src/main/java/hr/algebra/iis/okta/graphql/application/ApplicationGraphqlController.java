package hr.algebra.iis.okta.graphql.application;

import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.provider.ApplicationProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ApplicationGraphqlController {

    private final ApplicationProvider applicationProvider;

    public ApplicationGraphqlController(ApplicationProvider applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public List<ApplicationResponse> applications() {
        return applicationProvider.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public ApplicationResponse application(@Argument String id) {
        return applicationProvider.findById(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ApplicationResponse createApplication(@Argument ApplicationGraphqlInput input) {
        return applicationProvider.create(input.toApplicationRequest());
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ApplicationResponse updateApplication(@Argument String id, @Argument ApplicationGraphqlInput input) {
        return applicationProvider.update(id, input.toApplicationRequest());
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public DeleteApplicationPayload deleteApplication(@Argument String id) {
        applicationProvider.delete(id);
        return DeleteApplicationPayload.deleted(id);
    }
}
