package hr.algebra.iis.okta.security.bootstrap;

import hr.algebra.iis.okta.security.role.UserRole;
import hr.algebra.iis.okta.security.user.AppUserEntity;
import hr.algebra.iis.okta.security.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("reader", "reader123", UserRole.READ_ONLY);
        createIfMissing("admin", "admin123", UserRole.FULL_ACCESS);
    }

    private void createIfMissing(String username, String rawPassword, UserRole role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        appUserRepository.save(new AppUserEntity(username, passwordEncoder.encode(rawPassword), role));
    }
}
