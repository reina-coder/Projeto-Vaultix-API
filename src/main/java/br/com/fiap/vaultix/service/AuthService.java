package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.Usuario;
import br.com.fiap.vaultix.dto.LoginRequest;
import br.com.fiap.vaultix.dto.RegisterRequest;
import br.com.fiap.vaultix.dto.TokenResponse;
import br.com.fiap.vaultix.exception.BusinessException;
import br.com.fiap.vaultix.repository.UsuarioRepository;
import br.com.fiap.vaultix.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository repo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public TokenResponse registrar(RegisterRequest req) {
        if (repo.existsByUsername(req.username())) {
            throw new BusinessException("Username já cadastrado.");
        }
        Usuario u = Usuario.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role("USER")
                .build();
        repo.save(u);
        String token = jwtService.gerarToken(u.getUsername());
        return TokenResponse.of(token, jwtService.getExpirationSeconds());
    }

    public TokenResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        String token = jwtService.gerarToken(req.username());
        return TokenResponse.of(token, jwtService.getExpirationSeconds());
    }
}
