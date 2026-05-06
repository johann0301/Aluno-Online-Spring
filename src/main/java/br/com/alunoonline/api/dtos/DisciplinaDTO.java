package br.com.alunoonline.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisciplinaDTO {

    @NotBlank(message = "O nome não pode estar em branco")
    private String nome;

    @NotNull(message = "A carga horária não pode estar em branco")
    private Integer email;
}
