-- =====================================================================
-- VIEWS - aluno-online
-- Assunto BD2: Views (obrigatorio)
-- Rodar: psql -U postgres -d aluno-online -f 01_views.sql
-- =====================================================================

-- View 1: Aluno completo com contagem de matriculas
CREATE OR REPLACE VIEW vw_aluno_completo AS
SELECT
    a.id              AS aluno_id,
    a.name            AS aluno_nome,
    a.email           AS aluno_email,
    a.cpf             AS aluno_cpf,
    COUNT(m.id)       AS total_matriculas,
    COUNT(*) FILTER (WHERE m.status = 'MATRICULADO')      AS matriculas_ativas,
    COUNT(*) FILTER (WHERE m.status = 'APROVADO')  AS matriculas_concluidas,
    COUNT(*) FILTER (WHERE m.status = 'TRANCADO')   AS matriculas_trancadas
FROM aluno a
LEFT JOIN matricula m ON m.aluno_id = a.id
GROUP BY a.id, a.name, a.email, a.cpf
;;

-- View 2: Matricula com dados desnormalizados (relatorio detalhado)
CREATE OR REPLACE VIEW vw_matricula_detalhada AS
SELECT
    m.id                AS matricula_id,
    a.id                AS aluno_id,
    a.name              AS aluno_nome,
    a.cpf               AS aluno_cpf,
    d.id                AS disciplina_id,
    d.nome              AS disciplina_nome,
    d.carga_horaria     AS disciplina_carga_horaria,
    p.id                AS professor_id,
    p.name              AS professor_nome,
    m.data_primeira     AS data_matricula,
    m.nota1             AS nota1,
    m.nota2             AS nota2,
    ((m.nota1 + m.nota2) / 2) AS nota,
    m.status            AS status,
    CASE
        WHEN m.nota1 IS NULL OR m.nota2 IS NULL THEN 'SEM_NOTA'
        WHEN ((m.nota1 + m.nota2) / 2) >= 7 THEN 'APROVADO'
        WHEN ((m.nota1 + m.nota2) / 2) >= 5 THEN 'RECUPERACAO'
        ELSE 'REPROVADO'
    END AS situacao
FROM matricula m
INNER JOIN aluno a       ON a.id = m.aluno_id
INNER JOIN disciplina d  ON d.id = m.disciplina_id
LEFT  JOIN professor p   ON p.id = d.professor_id
;;

-- View 3: Aprovacao agregada por disciplina
CREATE OR REPLACE VIEW vw_aprovacao_disciplina AS
SELECT
    d.id                                            AS disciplina_id,
    d.nome                                          AS disciplina_nome,
    d.carga_horaria                                 AS carga_horaria,
    p.name                                          AS professor_nome,
    COUNT(m.id)                                     AS total_matriculas,
    COUNT(*) FILTER (WHERE ((m.nota1 + m.nota2) / 2) >= 7) AS aprovados,
    COUNT(*) FILTER (WHERE ((m.nota1 + m.nota2) / 2) < 7)  AS reprovados,
    ROUND(AVG((m.nota1 + m.nota2) / 2)::numeric, 2) AS media_nota,
    ROUND(
        100.0 * COUNT(*) FILTER (WHERE ((m.nota1 + m.nota2) / 2) >= 7)
        / NULLIF(COUNT(m.nota1), 0), 2
    )                                               AS percentual_aprovacao
FROM disciplina d
LEFT JOIN professor p   ON p.id = d.professor_id
LEFT JOIN matricula m   ON m.disciplina_id = d.id
GROUP BY d.id, d.nome, d.carga_horaria, p.name
;;

-- View 4: Carga / desempenho por professor
CREATE OR REPLACE VIEW vw_carga_professor AS
SELECT
    p.id                                                AS professor_id,
    p.name                                              AS professor_nome,
    p.email                                             AS professor_email,
    COALESCE(dados_disciplina.qtd_disciplinas, 0)       AS qtd_disciplinas,
    COALESCE(dados_disciplina.carga_horaria_total, 0)   AS carga_horaria_total,
    COALESCE(dados_matricula.total_matriculas, 0)       AS total_matriculas_lecionadas,
    dados_matricula.media_notas                         AS media_notas
FROM professor p
LEFT JOIN (
    SELECT
        professor_id,
        COUNT(id)                 AS qtd_disciplinas,
        SUM(carga_horaria)        AS carga_horaria_total
    FROM disciplina
    GROUP BY professor_id
) dados_disciplina ON dados_disciplina.professor_id = p.id
LEFT JOIN (
    SELECT
        d.professor_id,
        COUNT(m.id)                    AS total_matriculas,
        ROUND(AVG((m.nota1 + m.nota2) / 2)::numeric, 2) AS media_notas
    FROM disciplina d
    LEFT JOIN matricula m ON m.disciplina_id = d.id
    GROUP BY d.professor_id
) dados_matricula ON dados_matricula.professor_id = p.id
;;
