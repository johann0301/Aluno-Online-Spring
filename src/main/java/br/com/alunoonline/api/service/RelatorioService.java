package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.RelatorioRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    // Whitelist: views permitidas, colunas validas, e labels amigaveis.
    // Bloqueia SQL injection - so nomes daqui entram na query.
    private static final Map<String, ViewMeta> VIEWS = new LinkedHashMap<>();
    static {
        LinkedHashMap<String, String> v1 = new LinkedHashMap<>();
        v1.put("aluno_id",             "ID");
        v1.put("aluno_nome",           "Nome");
        v1.put("aluno_email",          "E-mail");
        v1.put("aluno_cpf",            "CPF");
        v1.put("total_matriculas",     "Total de Matrículas");
        v1.put("matriculas_ativas",    "Ativas");
        v1.put("matriculas_concluidas","Concluídas");
        v1.put("matriculas_trancadas", "Trancadas");
        VIEWS.put("vw_aluno_completo", new ViewMeta("Alunos - Visão Geral", v1));

        LinkedHashMap<String, String> v2 = new LinkedHashMap<>();
        v2.put("matricula_id",            "ID Matrícula");
        v2.put("aluno_id",                "ID Aluno");
        v2.put("aluno_nome",              "Aluno");
        v2.put("aluno_cpf",               "CPF");
        v2.put("disciplina_id",           "ID Disciplina");
        v2.put("disciplina_nome",         "Disciplina");
        v2.put("disciplina_carga_horaria","Carga Horária");
        v2.put("professor_id",            "ID Professor");
        v2.put("professor_nome",          "Professor");
        v2.put("data_matricula",          "Data Matrícula");
        v2.put("nota",                    "Nota");
        v2.put("status",                  "Status");
        v2.put("situacao",                "Situação");
        VIEWS.put("vw_matricula_detalhada", new ViewMeta("Matrículas - Detalhado", v2));

        LinkedHashMap<String, String> v3 = new LinkedHashMap<>();
        v3.put("disciplina_id",        "ID");
        v3.put("disciplina_nome",      "Disciplina");
        v3.put("carga_horaria",        "Carga Horária");
        v3.put("professor_nome",       "Professor");
        v3.put("total_matriculas",     "Total Matrículas");
        v3.put("aprovados",            "Aprovados");
        v3.put("reprovados",           "Reprovados");
        v3.put("media_nota",           "Média de Nota");
        v3.put("percentual_aprovacao", "% Aprovação");
        VIEWS.put("vw_aprovacao_disciplina", new ViewMeta("Aprovação por Disciplina", v3));

        LinkedHashMap<String, String> v4 = new LinkedHashMap<>();
        v4.put("professor_id",                "ID");
        v4.put("professor_nome",              "Professor");
        v4.put("professor_email",             "E-mail");
        v4.put("qtd_disciplinas",             "Qtd Disciplinas");
        v4.put("carga_horaria_total",         "Carga Horária Total");
        v4.put("total_matriculas_lecionadas", "Matrículas Lecionadas");
        v4.put("media_notas",                 "Média de Notas");
        VIEWS.put("vw_carga_professor", new ViewMeta("Professores - Carga e Desempenho", v4));
    }

    public record ViewMeta(String label, Map<String, String> colunas) {}

    private static final Set<String> OPERADORES =
        Set.of("=", "!=", ">", "<", ">=", "<=", "LIKE", "ILIKE");

    private final JdbcTemplate jdbc;

    @Autowired
    public RelatorioService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, ViewMeta> metadata() {
        return VIEWS;
    }

    public RelatorioResultado consultar(RelatorioRequestDTO req) {
        String view = req.getView();
        if (view == null || !VIEWS.containsKey(view)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "View invalida: " + view);
        }
        Set<String> colunasValidas = VIEWS.get(view).colunas().keySet();

        List<String> campos = (req.getCampos() == null || req.getCampos().isEmpty())
            ? new ArrayList<>(VIEWS.get(view).colunas().keySet())
            : req.getCampos();

        for (String c : campos) {
            if (!colunasValidas.contains(c)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Campo invalido: " + c);
            }
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", campos));
        sql.append(" FROM ").append(view);

        List<Object> params = new ArrayList<>();
        if (req.getFiltros() != null && !req.getFiltros().isEmpty()) {
            sql.append(" WHERE ");
            List<String> clauses = new ArrayList<>();
            for (RelatorioRequestDTO.Filtro f : req.getFiltros()) {
                if (!colunasValidas.contains(f.getCampo())) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Campo de filtro invalido: " + f.getCampo());
                }
                String op = f.getOperador() == null ? "=" : f.getOperador().toUpperCase();
                if (!OPERADORES.contains(op)) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Operador invalido: " + op);
                }
                clauses.add(f.getCampo() + " " + op + " ?");
                params.add(f.getValor());
            }
            sql.append(String.join(" AND ", clauses));
        }

        if (req.getOrderBy() != null && !req.getOrderBy().isBlank()) {
            if (!colunasValidas.contains(req.getOrderBy())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "orderBy invalido: " + req.getOrderBy());
            }
            String dir = "DESC".equalsIgnoreCase(req.getOrderDir()) ? "DESC" : "ASC";
            sql.append(" ORDER BY ").append(req.getOrderBy()).append(" ").append(dir);
        }

        if (req.getLimit() != null && req.getLimit() > 0 && req.getLimit() <= 10000) {
            sql.append(" LIMIT ").append(req.getLimit());
        }

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());

        Map<String, String> labelMap = VIEWS.get(view).colunas();
        List<String> labels = campos.stream()
            .map(c -> labelMap.getOrDefault(c, c))
            .collect(Collectors.toList());

        return new RelatorioResultado(campos, labels, rows, sql.toString());
    }

    public record RelatorioResultado(
        List<String> campos,
        List<String> labels,
        List<Map<String, Object>> linhas,
        String sqlExecutado
    ) {}
}
