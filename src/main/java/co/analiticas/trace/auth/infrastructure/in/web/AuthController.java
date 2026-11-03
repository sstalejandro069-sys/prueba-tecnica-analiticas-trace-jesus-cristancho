package co.analiticas.trace.auth.infrastructure.in.web;

import co.analiticas.trace.auth.application.port.in.LoginUseCase;
import co.analiticas.trace.auth.application.port.in.LogoutUseCase;
import co.analiticas.trace.auth.domain.Credentials;
import co.analiticas.trace.shared.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio y cierre de sesión con credenciales simuladas")
public class AuthController {

    private final LoginUseCase login;
    private final LogoutUseCase logout;

    public AuthController(LoginUseCase login, LogoutUseCase logout) {
        this.login = login;
        this.logout = logout;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión",
            description = "Valida usuario y contraseña y devuelve un token Bearer. Credenciales por defecto: admin / admin123.")
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "400", description = "Campos vacíos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var token = login.login(new Credentials(request.username(), request.password()));
        return LoginResponse.from(token);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token actual.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    @ApiResponse(responseCode = "401", description = "Token ausente o inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        logout.logout(BearerToken.extract(authorization));
        return ResponseEntity.noContent().build();
    }
}
