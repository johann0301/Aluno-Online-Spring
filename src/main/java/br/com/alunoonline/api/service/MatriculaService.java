package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.AlunoDTO;
import br.com.alunoonline.api.dtos.AtualizarNotasRequestDTO;
import br.com.alunoonline.api.dtos.DisciplinaDTO;
import br.com.alunoonline.api.dtos.DisciplinasAlunoResponseDTO;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponseDTO;
import br.com.alunoonline.api.dtos.MatriculaDTO;
import br.com.alunoonline.api.dtos.MatriculaResponseDTO;
import br.com.alunoonline.api.model.Aluno;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.model.Matricula;
import br.com.alunoonline.api.model.StatusMatricula;
import br.com.alunoonline.api.repository.AlunoRepository;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import br.com.alunoonline.api.repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatriculaService {

    private static final Double MEDIA_PARA_APROVACAO = 7.0;

    @Autowired
    MatriculaRepository matriculaRepository;

    @Autowired
    AlunoRepository alunoRepository;

    @Autowired
    DisciplinaRepository disciplinaRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
        matricula.setStatus(StatusMatricula.MATRICULADO);
        matricula.setDataPrimeira(LocalDateTime.now());

        Matricula matriculaSalva = matriculaRepository.save(matricula);
        return converterParaResponseDTO(matriculaSalva);
    }

    public void trancarMatricula(Long id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matricula não encontrada"));

        if (matricula.getStatus().equals(StatusMatricula.MATRICULADO)) {
            matricula.setStatus(StatusMatricula.TRANCADO);
            matriculaRepository.save(matricula);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só é possível trancar com status MATRICULADO");
        }
    }

    public void atualizarNotas(Long id, AtualizarNotasRequestDTO dto) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matricula não encontrada"));

        if (dto.getNota1() != null) matricula.setNota1(dto.getNota1());
        if (dto.getNota2() != null) matricula.setNota2(dto.getNota2());

        if (matricula.getNota1() != null && matricula.getNota2() != null) {
            Double media = (matricula.getNota1() + matricula.getNota2()) / 2;
            matricula.setStatus(media >= MEDIA_PARA_APROVACAO ? StatusMatricula.APROVADO : StatusMatricula.REPROVADO);
        }

        matriculaRepository.save(matricula);
    }

    public HistoricoAlunoResponseDTO emitirHistorico(Long alunoId) {
        List<Matricula> matriculas = matriculaRepository.findByAlunoId(alunoId);

        if (matriculas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma matrícula encontrada para esse aluno");
        }

        Aluno aluno = matriculas.get(0).getAluno();

        List<DisciplinasAlunoResponseDTO> disciplinas = new ArrayList<>();

        for (Matricula matricula : matriculas) {
            DisciplinasAlunoResponseDTO disc = new DisciplinasAlunoResponseDTO();
            disc.setNomeDisciplina(matricula.getDisciplina().getNome());
            
            if (matricula.getDisciplina().getProfessor() != null) {
                disc.setNomeProfessor(matricula.getDisciplina().getProfessor().getName());
            }

            disc.setNota1(matricula.getNota1());
            disc.setNota2(matricula.getNota2());

            if (matricula.getNota1() != null && matricula.getNota2() != null) {
                Double media = (matricula.getNota1() + matricula.getNota2()) / 2;
                disc.setMedia(media);
            }

            disc.setStatus(matricula.getStatus());
            disciplinas.add(disc);
        }

        HistoricoAlunoResponseDTO historico = new HistoricoAlunoResponseDTO();
        historico.setNomeAluno(aluno.getName());
        historico.setEmailAluno(aluno.getEmail());
        historico.setCpfAluno(aluno.getCpf());
        historico.setDisciplinas(disciplinas);

        return historico;
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

    public void transferirMatricula(Long matriculaId, Long disciplinaNovaId) {
        jdbcTemplate.update("CALL sp_transferir_matricula(?, ?)", matriculaId.intValue(), disciplinaNovaId.intValue());
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
        responseDTO.setNota1(matricula.getNota1());
        responseDTO.setNota2(matricula.getNota2());
        
        if (matricula.getNota1() != null && matricula.getNota2() != null) {
            Double media = (matricula.getNota1() + matricula.getNota2()) / 2;
            responseDTO.setMedia(media);
        }
        
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
