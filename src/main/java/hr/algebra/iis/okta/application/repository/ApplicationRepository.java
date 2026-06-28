package hr.algebra.iis.okta.application.repository;

import hr.algebra.iis.okta.application.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    boolean existsByExternalId(String externalId);
}
