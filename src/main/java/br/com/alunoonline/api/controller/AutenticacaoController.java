package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.DadosAutenticacao;
import br.com.alunoonline.api.infra.security.DadosTokenJWT;
import br.com.alunoonline.api.infra.security.TokenService;
import br.com.alunoonline.api.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AutenticacaoController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);
        String token = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new DadosTokenJWT(token));
    }
}
