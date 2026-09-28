package br.com.fiap.vaultix.dto;

public record TokenResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {
    public static TokenResponse of(String token, long expiresInSeconds) {
        return new TokenResponse(token, "Bearer", expiresInSeconds);
    }
}
