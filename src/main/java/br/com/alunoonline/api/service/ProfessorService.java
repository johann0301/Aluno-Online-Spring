package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.ProfessorDTO;
import br.com.alunoonline.api.model.Professor;
import br.com.alunoonline.api.repository.ProfessorRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    @Autowired
    ProfessorRepository professorRepository;

    public void criarProfessor(ProfessorDTO professorDTO) {
        Professor professor = new Professor();
        BeanUtils.copyProperties(professorDTO, professor);
        professorRepository.save(professor);
    }

    public List<Professor> listarTodosProfessores() {
        return professorRepository.findAll();
    }

    public Optional<Professor> buscarProfessorPorId(Long id) {
        return professorRepository.findById(id);
    }

    public void deletarProfessorPorId(Long id) {
        professorRepository.deleteById(id);
    }

    public void atualizarProfessor(Long id, ProfessorDTO professorDTO) {
        Optional<Professor> professorExistente = professorRepository.findById(id);

        if (professorExistente.isPresent()) {
            Professor professor = professorExistente.get();
            BeanUtils.copyProperties(professorDTO, professor);
            professor.setId(id);
            professorRepository.save(professor);
        }
    }
}
