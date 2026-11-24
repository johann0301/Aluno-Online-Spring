package br.com.alunoonline.api.dtos;

import br.com.alunoonline.api.model.StatusMatricula;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatriculaResponseDTO {

    private Long id;

    private AlunoDTO aluno;

    private DisciplinaDTO disciplina;

    private LocalDateTime dataPrimeira;

    private Double nota1;

    private Double nota2;
    
    private Double media;

    private StatusMatricula status;
}