package co.analiticas.trace.auth.infrastructure.in.web;

import co.analiticas.trace.auth.application.port.in.ValidateTokenUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Adaptador de entrada que protege /api/** validando el token Bearer. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER = "authenticatedUser";

    private final ValidateTokenUseCase validateToken;

    public AuthInterceptor(ValidateTokenUseCase validateToken) {
        this.validateToken = validateToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        var username = validateToken.validate(BearerToken.extract(request.getHeader(HttpHeaders.AUTHORIZATION)));
        request.setAttribute(AUTHENTICATED_USER, username);
        return true;
    }
}
