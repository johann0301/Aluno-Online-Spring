package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.AtualizarNotasRequestDTO;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponseDTO;
import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.service.MatriculaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
@SecurityRequirement(name = "bearer-key")
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

    @PatchMapping("/trancar/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trancarMatricula(@PathVariable Long id) {
        matriculaService.trancarMatricula(id);
    }

    @PatchMapping("/atualizar-notas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarNotas(@PathVariable Long id, @RequestBody AtualizarNotasRequestDTO dto) {
        matriculaService.atualizarNotas(id, dto);
    }

    @PostMapping("/{id}/transferir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferirMatricula(@PathVariable Long id, @RequestBody java.util.Map<String, Long> request) {
        matriculaService.transferirMatricula(id, request.get("disciplinaNovaId"));
    }

    @GetMapping("/emitir-historico/{alunoId}")
    @ResponseStatus(HttpStatus.OK)
    public HistoricoAlunoResponseDTO emitirHistorico(@PathVariable Long alunoId) {
        return matriculaService.emitirHistorico(alunoId);
    }
}
