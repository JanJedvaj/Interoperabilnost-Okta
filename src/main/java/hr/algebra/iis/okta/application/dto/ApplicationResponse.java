package hr.algebra.iis.okta.application.dto;

import hr.algebra.iis.okta.application.model.ApplicationEntity;

import java.time.OffsetDateTime;

public record ApplicationResponse(
        Long id,
        String externalId,
        String name,
        String label,
        String status,
        String signOnMode,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public String resourceId() {
        if (id != null) {
            return id.toString();
        }
        return externalId;
    }

    public static ApplicationResponse fromEntity(ApplicationEntity entity) {
        return new ApplicationResponse(
                entity.getId(),
                entity.getExternalId(),
                entity.getName(),
                entity.getLabel(),
                entity.getStatus(),
                entity.getSignOnMode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
