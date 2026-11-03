package co.analiticas.trace.auth.infrastructure.in.web;

import co.analiticas.trace.auth.domain.AuthToken;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Sesión creada tras un login exitoso")
public record LoginResponse(
        @Schema(description = "Token Bearer para las demás peticiones", example = "3f1c2b7e-9a1d-4c55-8e0b-2f6a7d9c1e44")
        String token,
        @Schema(example = "admin")
        String username,
        @Schema(description = "Fecha de expiración del token (UTC)")
        Instant expiresAt) {

    static LoginResponse from(AuthToken token) {
        return new LoginResponse(token.value(), token.username(), token.expiresAt());
    }
}
