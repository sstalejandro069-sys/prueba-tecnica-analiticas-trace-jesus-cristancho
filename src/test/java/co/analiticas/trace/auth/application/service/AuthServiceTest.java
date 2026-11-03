package co.analiticas.trace.auth.application.service;

import co.analiticas.trace.auth.domain.Credentials;
import co.analiticas.trace.auth.domain.InvalidCredentialsException;
import co.analiticas.trace.auth.domain.UnauthorizedException;
import co.analiticas.trace.auth.domain.User;
import co.analiticas.trace.auth.infrastructure.out.BCryptPasswordHasher;
import co.analiticas.trace.auth.infrastructure.out.InMemoryTokenStore;
import co.analiticas.trace.auth.infrastructure.out.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final InMemoryTokenStore tokens = new InMemoryTokenStore();
    private MutableClock clock;
    private AuthService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-03T12:00:00Z"));
        var users = new InMemoryUserRepository(new User("admin", hasher.hash("admin123")));
        service = new AuthService(users, tokens, hasher, clock, Duration.ofMinutes(60));
    }

    @Test
    void loginWithValidCredentialsReturnsUsableToken() {
        var token = service.login(new Credentials("admin", "admin123"));

        assertThat(token.username()).isEqualTo("admin");
        assertThat(service.validate(token.value())).isEqualTo("admin");
    }

    @Test
    void loginWithWrongPasswordFails() {
        assertThatThrownBy(() -> service.login(new Credentials("admin", "otra")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Usuario o contraseña incorrectos.");
    }

    @Test
    void loginWithUnknownUserFailsWithSameMessage() {
        assertThatThrownBy(() -> service.login(new Credentials("nadie", "admin123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Usuario o contraseña incorrectos.");
    }

    @Test
    void blankCredentialsAreRejected() {
        assertThatThrownBy(() -> new Credentials(" ", "x")).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        var token = service.login(new Credentials("admin", "admin123"));
        clock.advance(Duration.ofMinutes(61));

        assertThatThrownBy(() -> service.validate(token.value()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expiró");
    }

    @Test
    void logoutInvalidatesToken() {
        var token = service.login(new Credentials("admin", "admin123"));
        service.logout(token.value());

        assertThatThrownBy(() -> service.validate(token.value())).isInstanceOf(UnauthorizedException.class);
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
