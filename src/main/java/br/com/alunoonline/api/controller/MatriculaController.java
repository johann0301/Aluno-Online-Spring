package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}