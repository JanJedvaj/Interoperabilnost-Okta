package hr.algebra.iis.okta.security.auth;

import hr.algebra.iis.okta.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return tokensFor(userDetails);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isValidRefreshToken(request.refreshToken(), userDetails)) {
            throw new IllegalArgumentException("Refresh token is not valid");
        }

        return tokensFor(userDetails);
    }

    private AuthResponse tokensFor(UserDetails userDetails) {
        return AuthResponse.bearer(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }
}
