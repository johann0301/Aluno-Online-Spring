package br.com.alunoonline.api.infra.exception;

import br.com.alunoonline.api.controller.AutenticacaoController;
import br.com.alunoonline.api.controller.UsuarioController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {AutenticacaoController.class, UsuarioController.class})
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErro400(MethodArgumentNotValidException exception) {
        var erros = exception.getFieldErrors().stream()
                .map(erro -> new DadosErroValidacao(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> tratarErroAutenticacao() {
        return ResponseEntity.status(401).body(Map.of("erro", "Login ou senha inválidos."));
    }

    public record DadosErroValidacao(String campo, String mensagem) {
    }
}
