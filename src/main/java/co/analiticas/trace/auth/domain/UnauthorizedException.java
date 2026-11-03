package co.analiticas.trace.auth.domain;

import co.analiticas.trace.shared.domain.DomainException;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
