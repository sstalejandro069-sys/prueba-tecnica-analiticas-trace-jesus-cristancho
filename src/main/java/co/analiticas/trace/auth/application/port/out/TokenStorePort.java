package co.analiticas.trace.auth.application.port.out;

import co.analiticas.trace.auth.domain.AuthToken;

import java.util.Optional;

public interface TokenStorePort {

    void save(AuthToken token);

    Optional<AuthToken> find(String value);

    void remove(String value);
}
