package co.analiticas.trace.auth.application.port.in;

import co.analiticas.trace.auth.domain.AuthToken;
import co.analiticas.trace.auth.domain.Credentials;

public interface LoginUseCase {

    AuthToken login(Credentials credentials);
}
