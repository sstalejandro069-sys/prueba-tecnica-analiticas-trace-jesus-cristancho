package co.analiticas.trace.auth.infrastructure.in.web;

final class BearerToken {

    private static final String PREFIX = "Bearer ";

    private BearerToken() {
    }

    static String extract(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return null;
        }
        return authorizationHeader.substring(PREFIX.length()).trim();
    }
}
