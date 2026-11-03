package co.analiticas.trace.auth.application.port.out;

import co.analiticas.trace.auth.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);
}
