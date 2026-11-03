package co.analiticas.trace.auth.infrastructure.out;

import co.analiticas.trace.auth.application.port.out.TokenStorePort;
import co.analiticas.trace.auth.domain.AuthToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStore implements TokenStorePort {

    private final Map<String, AuthToken> tokens = new ConcurrentHashMap<>();

    @Override
    public void save(AuthToken token) {
        tokens.put(token.value(), token);
    }

    @Override
    public Optional<AuthToken> find(String value) {
        return Optional.ofNullable(tokens.get(value));
    }

    @Override
    public void remove(String value) {
        tokens.remove(value);
    }
}
