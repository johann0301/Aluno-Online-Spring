package br.com.alunoonline.api.infra.security;

import br.com.alunoonline.api.model.Usuario;
import br.com.alunoonline.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            try {
                String token = recuperarToken(authorization);
                Usuario usuario = repository.findByLogin(tokenService.getSubject(token));
                if (usuario == null) {
                    throw new BadCredentialsException("Usuário não encontrado.");
                }
                var authentication = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException exception) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(String authorization) {
        if (!authorization.startsWith("Bearer ")) {
            throw new BadCredentialsException("Cabeçalho de autorização inválido.");
        }
        String token = authorization.substring(7);
        if (token.isBlank()) {
            throw new BadCredentialsException("Token não informado.");
        }
        return token;
    }
}
