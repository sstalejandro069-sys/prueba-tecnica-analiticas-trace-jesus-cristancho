package co.analiticas.trace.auth.domain;

import co.analiticas.trace.shared.domain.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
