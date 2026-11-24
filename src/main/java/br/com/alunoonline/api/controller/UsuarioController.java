package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.DadosCadastroUsuario;
import br.com.alunoonline.api.dtos.DadosDetalhamentoUsuario;
import br.com.alunoonline.api.model.Usuario;
import br.com.alunoonline.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados,
                                      UriComponentsBuilder uriBuilder) {
        if (repository.findByLogin(dados.login()) != null) {
            return ResponseEntity.badRequest().body("Login já cadastrado.");
        }
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body("A senha deve ter no máximo 72 bytes em UTF-8.");
        }
        String senhaCriptografada = passwordEncoder.encode(dados.senha());
        Usuario usuario = repository.save(new Usuario(dados.login(), senhaCriptografada));
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoUsuario(usuario));
    }
}
