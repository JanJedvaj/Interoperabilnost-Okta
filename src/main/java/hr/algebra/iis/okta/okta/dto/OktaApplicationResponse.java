package hr.algebra.iis.okta.okta.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record OktaApplicationResponse(
        String id,
        String name,
        String label,
        String status,
        String signOnMode,
        OffsetDateTime created,
        OffsetDateTime lastUpdated,
        JsonNode raw
) {
}
