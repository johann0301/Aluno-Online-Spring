package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.DisciplinaDTO;
import br.com.alunoonline.api.service.DisciplinaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disciplina")
public class DisciplinaController {

    @Autowired
    DisciplinaService disciplinaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criarDisciplina(@RequestBody @Valid DisciplinaDTO disciplinaDTO) {
        disciplinaService.criarProfessor(disciplinaDTO);
    }
}
