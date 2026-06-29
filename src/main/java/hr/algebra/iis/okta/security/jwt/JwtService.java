package hr.algebra.iis.okta.security.jwt;

import hr.algebra.iis.okta.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ROLES_CLAIM = "roles";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        secretKey = Keys.hmacShaKeyFor(sha256(jwtProperties.secret()));
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, JwtTokenType.ACCESS, jwtProperties.accessTokenMinutes(), ChronoUnit.MINUTES);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, JwtTokenType.REFRESH, jwtProperties.refreshTokenDays(), ChronoUnit.DAYS);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isValidAccessToken(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, JwtTokenType.ACCESS);
    }

    public boolean isValidRefreshToken(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, JwtTokenType.REFRESH);
    }

    private String generateToken(UserDetails userDetails, JwtTokenType tokenType, long amount, ChronoUnit unit) {
        Instant now = Instant.now();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(ROLES_CLAIM, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(amount, unit)))
                .signWith(secretKey)
                .compact();
    }

    private boolean isTokenValid(String token, UserDetails userDetails, JwtTokenType expectedType) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        Date expiration = claims.getExpiration();

        return username.equals(userDetails.getUsername())
                && expectedType.name().equals(tokenType)
                && expiration.after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
