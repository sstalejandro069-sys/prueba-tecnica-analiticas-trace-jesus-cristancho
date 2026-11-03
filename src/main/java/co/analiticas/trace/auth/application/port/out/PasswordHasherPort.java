package co.analiticas.trace.auth.application.port.out;

public interface PasswordHasherPort {

    boolean matches(String rawPassword, String passwordHash);
}
