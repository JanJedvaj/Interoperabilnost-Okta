package hr.algebra.iis.okta.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenMinutes,
        long refreshTokenDays
) {

    public JwtProperties {
        secret = secret == null || secret.isBlank()
                ? "change-this-development-secret-before-using-the-application"
                : secret;
        accessTokenMinutes = accessTokenMinutes <= 0 ? 15 : accessTokenMinutes;
        refreshTokenDays = refreshTokenDays <= 0 ? 7 : refreshTokenDays;
    }
}
