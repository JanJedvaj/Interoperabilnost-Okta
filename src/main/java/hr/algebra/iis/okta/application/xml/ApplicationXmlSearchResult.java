package hr.algebra.iis.okta.application.xml;

import java.util.List;

public record ApplicationXmlSearchResult(
        String searchTerm,
        String sourceXml,
        boolean xmlValid,
        List<String> validationMessages,
        List<ApplicationXmlSearchItem> applications
) {
}
