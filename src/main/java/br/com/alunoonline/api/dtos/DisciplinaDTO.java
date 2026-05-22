package br.com.alunoonline.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DisciplinaDTO {

    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    private String nome;

    @NotNull(message = "A carga horária é obrigatória")
    @Positive(message = "A carga horária deve ser positiva")
    private Integer cargaHoraria;

    private Long professorId;
}
