package co.analiticas.trace.auth.domain;

public record Credentials(String username, String password) {

    public Credentials {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Usuario y contraseña son obligatorios.");
        }
        username = username.trim();
    }
}
