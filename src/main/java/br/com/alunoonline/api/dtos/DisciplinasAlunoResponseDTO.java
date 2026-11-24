package br.com.alunoonline.api.dtos;

import br.com.alunoonline.api.model.StatusMatricula;
import lombok.Data;

@Data
public class DisciplinasAlunoResponseDTO {
    private String nomeDisciplina;
    private String nomeProfessor;
    private Double nota1;
    private Double nota2;
    private Double media;
    private StatusMatricula status;
}
