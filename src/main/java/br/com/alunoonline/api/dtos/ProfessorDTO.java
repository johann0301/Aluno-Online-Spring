package br.com.alunoonline.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfessorDTO {

    @NotBlank(message = "O nome não pode estar em branco")
    private String name;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail não pode estar em branco")
    private String email;

    @NotBlank(message = "O CPF não pode estar em branco")
    private String cpf;
}
