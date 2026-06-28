package hr.algebra.iis.okta.okta.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OktaApplicationRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Label is required")
        String label,

        @NotBlank(message = "Sign-on mode is required")
        String signOnMode,

        @NotNull(message = "Settings are required")
        JsonNode settings
) {

    public static OktaApplicationRequest fromApplicationRequest(ApplicationRequest request) {
        return new OktaApplicationRequest(
                request.name(),
                request.label(),
                request.signOnMode(),
                JsonNodeFactory.instance.objectNode()
        );
    }
}
