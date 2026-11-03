package co.analiticas.trace.auth.domain;

import java.util.Objects;

public record User(String username, String passwordHash) {

    public User {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(passwordHash, "passwordHash");
    }
}
