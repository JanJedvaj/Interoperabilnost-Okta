package hr.algebra.iis.okta.config;

import hr.algebra.iis.okta.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String READ_ONLY = "READ_ONLY";
    private static final String FULL_ACCESS = "FULL_ACCESS";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/app.css", "/app.js", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/graphiql/**", "/graphql").hasAnyRole(READ_ONLY, FULL_ACCESS)
                        .requestMatchers("/ws/**").hasAnyRole(READ_ONLY, FULL_ACCESS)
                        .requestMatchers(HttpMethod.GET, "/api/weather/**").hasAnyRole(READ_ONLY, FULL_ACCESS)
                        .requestMatchers(HttpMethod.GET, "/api/applications/**", "/api/okta/applications/**")
                        .hasAnyRole(READ_ONLY, FULL_ACCESS)
                        .requestMatchers(HttpMethod.POST, "/api/applications/**", "/api/okta/applications/**")
                        .hasRole(FULL_ACCESS)
                        .requestMatchers(HttpMethod.PUT, "/api/applications/**", "/api/okta/applications/**")
                        .hasRole(FULL_ACCESS)
                        .requestMatchers(HttpMethod.DELETE, "/api/applications/**", "/api/okta/applications/**")
                        .hasRole(FULL_ACCESS)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return authenticationProvider::authenticate;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
