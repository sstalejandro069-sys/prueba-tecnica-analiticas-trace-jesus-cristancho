package co.analiticas.trace.auth.application.port.in;

/** Devuelve el usuario dueño del token o lanza UnauthorizedException. */
public interface ValidateTokenUseCase {

    String validate(String token);
}
