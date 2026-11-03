package co.analiticas.trace.auth.infrastructure.config;

import co.analiticas.trace.auth.application.port.out.TokenStorePort;
import co.analiticas.trace.auth.application.service.AuthService;
import co.analiticas.trace.auth.domain.User;
import co.analiticas.trace.auth.infrastructure.out.BCryptPasswordHasher;
import co.analiticas.trace.auth.infrastructure.out.InMemoryUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

/** Ensambla el hexágono de autenticación: la capa de aplicación no depende de Spring. */
@Configuration
public class AuthConfig {

    @Bean
    BCryptPasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    InMemoryUserRepository userRepository(BCryptPasswordHasher hasher,
                                          @Value("${trace.auth.default-username}") String username,
                                          @Value("${trace.auth.default-password}") String password) {
        return new InMemoryUserRepository(new User(username, hasher.hash(password)));
    }

    @Bean
    AuthService authService(InMemoryUserRepository users, TokenStorePort tokens, BCryptPasswordHasher hasher,
                            Clock clock, @Value("${trace.auth.token-ttl-minutes}") long ttlMinutes) {
        return new AuthService(users, tokens, hasher, clock, Duration.ofMinutes(ttlMinutes));
    }
}
