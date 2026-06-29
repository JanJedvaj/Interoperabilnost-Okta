package hr.algebra.iis.okta.graphql.application;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;

public record ApplicationGraphqlInput(
        String externalId,
        String name,
        String label,
        String status,
        String signOnMode
) {

    ApplicationRequest toApplicationRequest() {
        return new ApplicationRequest(externalId, name, label, status, signOnMode);
    }
}
