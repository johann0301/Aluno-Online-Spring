package br.com.alunoonline.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    @Autowired
    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> kpis() {
        Map<String, Object> r = new HashMap<>();

        r.put("totalAlunos",
            jdbc.queryForObject("SELECT COUNT(*) FROM aluno", Long.class));
        r.put("totalProfessores",
            jdbc.queryForObject("SELECT COUNT(*) FROM professor", Long.class));
        r.put("totalDisciplinas",
            jdbc.queryForObject("SELECT COUNT(*) FROM disciplina", Long.class));
        r.put("totalMatriculas",
            jdbc.queryForObject("SELECT COUNT(*) FROM matricula", Long.class));
        r.put("matriculasAtivas",
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM matricula WHERE status = 'ATIVA'", Long.class));
        r.put("matriculasConcluidas",
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM matricula WHERE status = 'CONCLUIDA'", Long.class));
        r.put("matriculasTrancadas",
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM matricula WHERE status = 'TRANCADA'", Long.class));

        // Media geral usando view
        r.put("mediaGeral",
            jdbc.queryForObject(
                "SELECT COALESCE(ROUND(AVG(media_nota), 2), 0) FROM vw_aprovacao_disciplina " +
                "WHERE media_nota IS NOT NULL", Double.class));

        // Top 5 disciplinas por total de matriculas (usa view)
        List<Map<String, Object>> topDisciplinas = jdbc.queryForList(
            "SELECT disciplina_nome, professor_nome, total_matriculas, " +
            "       media_nota, percentual_aprovacao " +
            "  FROM vw_aprovacao_disciplina " +
            " ORDER BY total_matriculas DESC " +
            " LIMIT 5");
        r.put("topDisciplinas", topDisciplinas);

        // Ranking professores por media de notas (usa view)
        List<Map<String, Object>> rankingProfessores = jdbc.queryForList(
            "SELECT professor_nome, qtd_disciplinas, carga_horaria_total, " +
            "       total_matriculas_lecionadas, media_notas " +
            "  FROM vw_carga_professor " +
            " WHERE media_notas IS NOT NULL " +
            " ORDER BY media_notas DESC " +
            " LIMIT 5");
        r.put("rankingProfessores", rankingProfessores);

        return r;
    }

    public List<Map<String, Object>> boletimAluno(Long alunoId) {
        return jdbc.queryForList(
            "SELECT * FROM vw_matricula_detalhada WHERE aluno_id = ? " +
            "ORDER BY disciplina_nome", alunoId);
    }

    public List<Map<String, Object>> matriculasAtivas() {
        return jdbc.queryForList(
            "SELECT matricula_id, aluno_nome, disciplina_nome, " +
            "       professor_nome, nota, status " +
            "  FROM vw_matricula_detalhada " +
            " WHERE status = 'ATIVA' " +
            " ORDER BY disciplina_nome, aluno_nome");
    }
}
