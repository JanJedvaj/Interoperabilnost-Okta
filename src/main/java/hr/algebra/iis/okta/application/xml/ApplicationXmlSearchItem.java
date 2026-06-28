package hr.algebra.iis.okta.application.xml;

public record ApplicationXmlSearchItem(
        String resourceId,
        Long localId,
        String externalId,
        String name,
        String label,
        String status,
        String signOnMode,
        String createdAt,
        String updatedAt
) {
}
