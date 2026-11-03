package co.analiticas.trace.shared.domain;

import java.util.List;

/**
 * Excepción base del dominio. Cada funcionalidad define sus propias
 * excepciones y el adaptador web las traduce a códigos HTTP.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;
    private final List<String> details;

    protected DomainException(String code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    protected DomainException(String code, String message) {
        this(code, message, List.of());
    }

    public String getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
