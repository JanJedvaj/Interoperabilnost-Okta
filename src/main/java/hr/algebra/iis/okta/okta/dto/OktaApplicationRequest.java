package hr.algebra.iis.okta.okta.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
}
