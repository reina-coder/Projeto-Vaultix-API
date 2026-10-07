package br.com.fiap.vaultix.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // O JwtService espera o secret em Base64 e precisa de pelo menos 256 bits.
        String secret = Base64.getEncoder().encodeToString(
                "chave-de-teste-vaultix-com-mais-de-256-bits-para-hmac".
                        getBytes(StandardCharsets.UTF_8)
        );

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
    }

    @Test
    void deveGerarTokenEExtrairUsername() {
        String token = jwtService.gerarToken("admin");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairUsername(token)).isEqualTo("admin");
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken("admin");
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        assertThatThrownBy(() -> jwtService.extrairUsername(adulterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", -60L);

        String token = jwtService.gerarToken("admin");

        assertThatThrownBy(() -> jwtService.extrairUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
