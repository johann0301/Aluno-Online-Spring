package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.AlunoDTO;
import br.com.alunoonline.api.dtos.DisciplinaDTO;
import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.model.Aluno;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.model.Matricula;
import br.com.alunoonline.api.repository.AlunoRepository;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import br.com.alunoonline.api.repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    @Autowired
    JdbcTemplate jdbcTemplate;

    // Chama procedure SQL sp_transferir_matricula
    public void transferirMatricula(Long matriculaId, Long disciplinaNovaId) {
        jdbcTemplate.update(
            "CALL sp_transferir_matricula(?, ?)",
            matriculaId.intValue(), disciplinaNovaId.intValue()
        );
    }

    // Chama procedure SQL sp_lancar_nota
    public void lancarNota(Long matriculaId, BigDecimal nota) {
        jdbcTemplate.update(
            "CALL sp_lancar_nota(?, ?)",
            matriculaId.intValue(), nota
        );
    }

    // Chama function SQL fn_media_aluno
    public BigDecimal mediaAluno(Long alunoId) {
        return jdbcTemplate.queryForObject(
            "SELECT fn_media_aluno(?)", BigDecimal.class, alunoId
        );
    }

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
        responseDTO.setAluno(converterAluno(matricula.getAluno()));
        responseDTO.setDisciplina(converterDisciplina(matricula.getDisciplina()));
        responseDTO.setDataPrimeira(matricula.getDataPrimeira());
        responseDTO.setNota(matricula.getNota());
        responseDTO.setStatus(matricula.getStatus());
        return responseDTO;
    }

    private AlunoDTO converterAluno(Aluno aluno) {
        if (aluno == null) return null;
        AlunoDTO dto = new AlunoDTO();
        dto.setId(aluno.getId());
        dto.setName(aluno.getName());
        dto.setEmail(aluno.getEmail());
        dto.setCpf(aluno.getCpf());
        return dto;
    }

    private DisciplinaDTO converterDisciplina(Disciplina disciplina) {
        if (disciplina == null) return null;
        DisciplinaDTO dto = new DisciplinaDTO();
        dto.setId(disciplina.getId());
        dto.setNome(disciplina.getNome());
        dto.setCargaHoraria(disciplina.getCargaHoraria());
        if (disciplina.getProfessor() != null) {
            dto.setProfessorId(disciplina.getProfessor().getId());
        }
        return dto;
    }
}
