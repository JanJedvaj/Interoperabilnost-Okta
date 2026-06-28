package hr.algebra.iis.okta.application.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class JsonApplicationImportValidator {

    private static final String SCHEMA_PATH = "schemas/application-schema.json";

    private final JsonSchema schema;

    public JsonApplicationImportValidator() throws IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        try (InputStream inputStream = new ClassPathResource(SCHEMA_PATH).getInputStream()) {
            schema = factory.getSchema(inputStream);
        }
    }

    public void validate(JsonNode document) {
        Set<ValidationMessage> validationMessages = schema.validate(document);
        if (validationMessages.isEmpty()) {
            return;
        }

        List<String> errors = validationMessages.stream()
                .map(ValidationMessage::getMessage)
                .sorted(Comparator.naturalOrder())
                .toList();

        throw new ApplicationImportValidationException(errors);
    }
}
