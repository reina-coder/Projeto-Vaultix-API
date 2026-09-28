package br.com.fiap.vaultix.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${vaultix.jwt.secret}")
    private String secret;

    @Value("${vaultix.jwt.expiration-seconds}")
    private long expirationSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String gerarToken(String username) {
        Date agora = new Date();
        Date expira = new Date(agora.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
                .subject(username)
                .issuedAt(agora)
                .expiration(expira)
                .signWith(key())
                .compact();
    }

    public String extrairUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
