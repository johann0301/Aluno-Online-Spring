package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.DisciplinaDTO;
import br.com.alunoonline.api.dtos.ProfessorDTO;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.model.Professor;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DisciplinaService {

    @Autowired
    DisciplinaRepository disciplinaRepository;

    public void criarProfessor(DisciplinaDTO disciplinaDTO) {
        Disciplina disciplina = new Disciplina();
        BeanUtils.copyProperties(disciplinaDTO, disciplina);
        disciplinaRepository.save(disciplina);
    }
}
