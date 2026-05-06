package br.com.alunoonline.api.dtos;

import br.com.alunoonline.api.model.StatusMatricula;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatriculaDTO {

    @NotNull(message = "ID do aluno não pode ser nulo")
    private Long alunoId;

    @NotNull(message = "ID da disciplina não pode ser nulo")
    private Long disciplinaId;

    @NotNull(message = "Status não pode ser nulo")
    private StatusMatricula status;
}