package co.analiticas.trace.auth.application.service;

import co.analiticas.trace.auth.application.port.in.LoginUseCase;
import co.analiticas.trace.auth.application.port.in.LogoutUseCase;
import co.analiticas.trace.auth.application.port.in.ValidateTokenUseCase;
import co.analiticas.trace.auth.application.port.out.PasswordHasherPort;
import co.analiticas.trace.auth.application.port.out.TokenStorePort;
import co.analiticas.trace.auth.application.port.out.UserRepositoryPort;
import co.analiticas.trace.auth.domain.AuthToken;
import co.analiticas.trace.auth.domain.Credentials;
import co.analiticas.trace.auth.domain.InvalidCredentialsException;
import co.analiticas.trace.auth.domain.UnauthorizedException;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public class AuthService implements LoginUseCase, ValidateTokenUseCase, LogoutUseCase {

    private final UserRepositoryPort users;
    private final TokenStorePort tokens;
    private final PasswordHasherPort hasher;
    private final Clock clock;
    private final Duration tokenTtl;

    public AuthService(UserRepositoryPort users, TokenStorePort tokens, PasswordHasherPort hasher,
                       Clock clock, Duration tokenTtl) {
        this.users = users;
        this.tokens = tokens;
        this.hasher = hasher;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    @Override
    public AuthToken login(Credentials credentials) {
        // Mismo mensaje para usuario inexistente o contraseña errada: no revela qué usuarios existen.
        var user = users.findByUsername(credentials.username())
                .filter(u -> hasher.matches(credentials.password(), u.passwordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Usuario o contraseña incorrectos."));

        var token = new AuthToken(UUID.randomUUID().toString(), user.username(), clock.instant().plus(tokenTtl));
        tokens.save(token);
        return token;
    }

    @Override
    public String validate(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new UnauthorizedException("Se requiere autenticación.");
        }
        var token = tokens.find(tokenValue)
                .orElseThrow(() -> new UnauthorizedException("La sesión no es válida. Inicie sesión de nuevo."));
        if (token.isExpired(clock.instant())) {
            tokens.remove(tokenValue);
            throw new UnauthorizedException("La sesión expiró. Inicie sesión de nuevo.");
        }
        return token.username();
    }

    @Override
    public void logout(String tokenValue) {
        if (tokenValue != null) {
            tokens.remove(tokenValue);
        }
    }
}
