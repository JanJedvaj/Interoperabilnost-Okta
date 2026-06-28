package hr.algebra.iis.okta.application.xml;

import java.util.List;

public record ApplicationXmlValidationResult(
        boolean valid,
        List<String> messages
) {

    public static ApplicationXmlValidationResult success() {
        return new ApplicationXmlValidationResult(true, List.of());
    }

    public static ApplicationXmlValidationResult invalid(List<String> messages) {
        return new ApplicationXmlValidationResult(false, List.copyOf(messages));
    }
}
