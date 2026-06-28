package hr.algebra.iis.okta.application.xml;

public record ApplicationXmlDocument(
        String xml,
        ApplicationXmlValidationResult validationResult
) {
}
