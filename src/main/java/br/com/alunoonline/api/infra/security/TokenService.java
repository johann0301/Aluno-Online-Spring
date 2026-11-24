package br.com.alunoonline.api.infra.security;

import br.com.alunoonline.api.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private static final String ISSUER = "API Aluno Online";

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuario.getLogin())
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(secret));
    }

    public String getSubject(String token) {
        try {
            String subject = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
            if (subject == null || subject.isBlank()) {
                throw new BadCredentialsException("Token inválido.");
            }
            return subject;
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsException("Token inválido ou expirado.", exception);
        }
    }
}
