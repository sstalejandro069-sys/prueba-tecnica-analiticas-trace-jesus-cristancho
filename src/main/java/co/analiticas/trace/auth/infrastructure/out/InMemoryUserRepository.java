package co.analiticas.trace.auth.infrastructure.out;

import co.analiticas.trace.auth.application.port.out.UserRepositoryPort;
import co.analiticas.trace.auth.domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Usuarios simulados para el PoC. Reemplazable por un adaptador JPA/LDAP sin tocar el dominio. */
public class InMemoryUserRepository implements UserRepositoryPort {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public InMemoryUserRepository(User... seed) {
        for (User user : seed) {
            users.put(user.username().toLowerCase(), user);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username.toLowerCase()));
    }
}
