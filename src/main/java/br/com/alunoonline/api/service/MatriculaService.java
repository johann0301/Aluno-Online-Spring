package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.model.Aluno;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.model.Matricula;
import br.com.alunoonline.api.repository.AlunoRepository;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import br.com.alunoonline.api.repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatriculaService {

    @Autowired
    MatriculaRepository matriculaRepository;

    @Autowired
    AlunoRepository alunoRepository;

    @Autowired
    DisciplinaRepository disciplinaRepository;

    public MatriculaResponseDTO criarMatricula(MatriculaDTO matriculaDTO) {
        Aluno aluno = alunoRepository.findById(matriculaDTO.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        Disciplina disciplina = disciplinaRepository.findById(matriculaDTO.getDisciplinaId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));

        Optional<Matricula> matriculaExistente = matriculaRepository
                .findByAlunoIdAndDisciplinaId(matriculaDTO.getAlunoId(), matriculaDTO.getDisciplinaId());

        if (matriculaExistente.isPresent()) {
            throw new RuntimeException("Aluno já está matriculado nesta disciplina");
        }

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setDisciplina(disciplina);
        matricula.setStatus(matriculaDTO.getStatus());
        matricula.setDataPrimeira(LocalDateTime.now());

        Matricula matriculaSalva = matriculaRepository.save(matricula);
        return converterParaResponseDTO(matriculaSalva);
    }

    public List<MatriculaResponseDTO> listarTodasMatriculas() {
        return matriculaRepository.findAll().stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    public MatriculaResponseDTO buscarMatriculaPorId(Long id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));
        return converterParaResponseDTO(matricula);
    }

    public List<MatriculaResponseDTO> buscarMatriculasPorAlunoId(Long alunoId) {
        return matriculaRepository.findByAlunoId(alunoId).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    public List<MatriculaResponseDTO> buscarMatriculasPorDisciplinaId(Long disciplinaId) {
        return matriculaRepository.findByDisciplinaId(disciplinaId).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    public void deletarMatriculaPorId(Long id) {
        matriculaRepository.deleteById(id);
    }

    public MatriculaResponseDTO atualizarMatricula(Long id, MatriculaDTO matriculaDTO) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        matricula.setStatus(matriculaDTO.getStatus());
        Matricula matriculaAtualizada = matriculaRepository.save(matricula);
        return converterParaResponseDTO(matriculaAtualizada);
    }

    private MatriculaResponseDTO converterParaResponseDTO(Matricula matricula) {
        MatriculaResponseDTO responseDTO = new MatriculaResponseDTO();
        responseDTO.setId(matricula.getId());
        responseDTO.setDataPrimeira(matricula.getDataPrimeira());
        responseDTO.setNota(matricula.getNota());
        responseDTO.setStatus(matricula.getStatus());
        return responseDTO;
    }
}