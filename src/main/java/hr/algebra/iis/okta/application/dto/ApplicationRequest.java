package hr.algebra.iis.okta.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @Size(max = 120, message = "External ID can contain at most 120 characters")
        String externalId,

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name can contain at most 120 characters")
        String name,

        @NotBlank(message = "Label is required")
        @Size(max = 200, message = "Label can contain at most 200 characters")
        String label,

        @NotBlank(message = "Status is required")
        @Size(max = 60, message = "Status can contain at most 60 characters")
        String status,

        @NotBlank(message = "Sign-on mode is required")
        @Size(max = 80, message = "Sign-on mode can contain at most 80 characters")
        String signOnMode
) {
}
