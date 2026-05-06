package br.com.alunoonline.api.dtos;

import br.com.alunoonline.api.model.StatusMatricula;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MatriculaResponseDTO {

    private Long id;

    private AlunoDTO aluno;

    private DisciplinaDTO disciplina;

    private LocalDateTime dataPrimeira;

    private BigDecimal nota;

    private StatusMatricula status;
}