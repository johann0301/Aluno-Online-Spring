package br.com.alunoonline.api.repository;

import br.com.alunoonline.api.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByAlunoId(Long alunoId);

    List<Matricula> findByDisciplinaId(Long disciplinaId);

    Optional<Matricula> findByAlunoIdAndDisciplinaId(Long alunoId, Long disciplinaId);
}