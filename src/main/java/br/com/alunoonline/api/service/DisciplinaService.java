package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.DisciplinaDTO;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.model.Professor;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import br.com.alunoonline.api.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DisciplinaService {

    @Autowired
    DisciplinaRepository disciplinaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    public Disciplina criarDisciplina(DisciplinaDTO dto) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome(dto.getNome());
        disciplina.setCargaHoraria(dto.getCargaHoraria());
        disciplina.setProfessor(resolverProfessor(dto.getProfessorId()));
        return disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> listarTodas() {
        return disciplinaRepository.findAll();
    }

    public Disciplina buscarPorId(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
    }

    public Disciplina atualizar(Long id, DisciplinaDTO dto) {
        Disciplina disciplina = buscarPorId(id);
        disciplina.setNome(dto.getNome());
        disciplina.setCargaHoraria(dto.getCargaHoraria());
        disciplina.setProfessor(resolverProfessor(dto.getProfessorId()));
        return disciplinaRepository.save(disciplina);
    }

    public void deletar(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada");
        }
        disciplinaRepository.deleteById(id);
    }

    private Professor resolverProfessor(Long professorId) {
        if (professorId == null) return null;
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professor não encontrado"));
    }
}
