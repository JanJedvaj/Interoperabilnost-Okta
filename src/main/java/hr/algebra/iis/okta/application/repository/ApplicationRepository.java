package hr.algebra.iis.okta.application.repository;

import hr.algebra.iis.okta.application.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    boolean existsByExternalId(String externalId);

    Optional<ApplicationEntity> findByExternalId(String externalId);
}
