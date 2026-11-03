package co.analiticas.trace.auth.infrastructure.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de acceso")
public record LoginRequest(
        @Schema(description = "Nombre de usuario", example = "admin")
        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @Schema(description = "Contraseña", example = "admin123")
        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
