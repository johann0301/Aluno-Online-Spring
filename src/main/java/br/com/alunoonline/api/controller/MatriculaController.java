package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    @Autowired
    MatriculaService matriculaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponseDTO criarMatricula(@RequestBody @Valid MatriculaDTO matriculaDTO) {
        return matriculaService.criarMatricula(matriculaDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MatriculaResponseDTO> listarTodasMatriculas() {
        return matriculaService.listarTodasMatriculas();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MatriculaResponseDTO buscarMatriculaPorId(@PathVariable Long id) {
        return matriculaService.buscarMatriculaPorId(id);
    }

    @GetMapping("/aluno/{alunoId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MatriculaResponseDTO> buscarMatriculasPorAluno(@PathVariable Long alunoId) {
        return matriculaService.buscarMatriculasPorAlunoId(alunoId);
    }

    @GetMapping("/disciplina/{disciplinaId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MatriculaResponseDTO> buscarMatriculasPorDisciplina(@PathVariable Long disciplinaId) {
        return matriculaService.buscarMatriculasPorDisciplinaId(disciplinaId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarMatricula(@PathVariable Long id, @RequestBody @Valid MatriculaDTO matriculaDTO) {
        matriculaService.atualizarMatricula(id, matriculaDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarMatricula(@PathVariable Long id) {
        matriculaService.deletarMatriculaPorId(id);
    }

    // Procedure SQL: sp_transferir_matricula
    @PostMapping("/{id}/transferir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferirMatricula(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        matriculaService.transferirMatricula(id, body.get("disciplinaNovaId"));
    }

    // Procedure SQL: sp_lancar_nota
    @PostMapping("/{id}/nota")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lancarNota(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {
        matriculaService.lancarNota(id, body.get("nota"));
    }

    // Function SQL: fn_media_aluno
    @GetMapping("/aluno/{alunoId}/media")
    public Map<String, BigDecimal> mediaAluno(@PathVariable Long alunoId) {
        return Map.of("media", matriculaService.mediaAluno(alunoId));
    }
}