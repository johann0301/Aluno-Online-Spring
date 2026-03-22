package br.com.alunoonline.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "aluno")
@Entity
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String cpf;

}
