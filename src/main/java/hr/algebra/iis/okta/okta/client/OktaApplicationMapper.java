package hr.algebra.iis.okta.okta.client;

import com.fasterxml.jackson.databind.JsonNode;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class OktaApplicationMapper {

    public OktaApplicationResponse toResponse(JsonNode node) {
        return new OktaApplicationResponse(
                textOrNull(node, "id"),
                textOrNull(node, "name"),
                textOrNull(node, "label"),
                textOrNull(node, "status"),
                textOrNull(node, "signOnMode"),
                dateTimeOrNull(node, "created"),
                dateTimeOrNull(node, "lastUpdated"),
                node
        );
    }

    private static String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private static OffsetDateTime dateTimeOrNull(JsonNode node, String fieldName) {
        String value = textOrNull(node, fieldName);
        if (value == null || value.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value);
    }
}
