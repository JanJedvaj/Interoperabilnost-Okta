package hr.algebra.iis.repository;

import hr.algebra.iis.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByExternalId(String externalId);
}
