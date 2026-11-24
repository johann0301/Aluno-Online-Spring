
-- =====================================================================
-- PROCEDURES - aluno-online
-- Assunto BD2: Procedures/Functions (2o assunto, complementa Views)
-- Rodar: psql -U postgres -d aluno-online -f 02_procedures.sql
-- =====================================================================

-- Procedure 1: Transferir matricula de uma disciplina para outra
-- Valida existencia, evita duplicata, mantem nota/status
CREATE OR REPLACE PROCEDURE sp_transferir_matricula(
    p_matricula_id        INTEGER,
    p_disciplina_nova_id  INTEGER
)
LANGUAGE plpgsql AS $$
DECLARE
    v_aluno_id              BIGINT;
    v_disciplina_atual_id   BIGINT;
BEGIN
    SELECT aluno_id, disciplina_id
    INTO v_aluno_id, v_disciplina_atual_id
    FROM matricula
    WHERE id = p_matricula_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Matricula % nao encontrada.', p_matricula_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM disciplina WHERE id = p_disciplina_nova_id) THEN
        RAISE EXCEPTION 'Disciplina % nao encontrada.', p_disciplina_nova_id;
    END IF;

    IF v_disciplina_atual_id = p_disciplina_nova_id THEN
        RAISE EXCEPTION 'A matricula ja esta na disciplina informada.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM matricula
        WHERE aluno_id = v_aluno_id
          AND disciplina_id = p_disciplina_nova_id
          AND id <> p_matricula_id
    ) THEN
        RAISE EXCEPTION
          'Aluno % ja possui matricula na disciplina %.',
          v_aluno_id, p_disciplina_nova_id;
    END IF;

    UPDATE matricula
       SET disciplina_id = p_disciplina_nova_id
     WHERE id = p_matricula_id;
END;
$$
;;

-- Procedure 2: Lancar nota e atualizar status automaticamente
-- media >= 7  -> APROVADO
-- media <  7  -> mantem status atual (ou TRANCADO se media = 0)
CREATE OR REPLACE PROCEDURE sp_lancar_nota(
    p_matricula_id INTEGER,
    p_nota         NUMERIC
)
LANGUAGE plpgsql AS $$
BEGIN
    IF p_nota < 0 OR p_nota > 10 THEN
        RAISE EXCEPTION 'Nota % invalida. Deve estar entre 0 e 10.', p_nota;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM matricula WHERE id = p_matricula_id) THEN
        RAISE EXCEPTION 'Matricula % nao encontrada.', p_matricula_id;
    END IF;

    UPDATE matricula
       SET nota1   = p_nota,
           nota2   = p_nota,
           status = CASE
                       WHEN p_nota >= 7 THEN 'APROVADO'::varchar
                       ELSE status
                   END
     WHERE id = p_matricula_id;
END;
$$
;;

-- Function: Retorna media geral de um aluno (usada em relatorios)
CREATE OR REPLACE FUNCTION fn_media_aluno(p_aluno_id BIGINT)
RETURNS NUMERIC
LANGUAGE plpgsql AS $$
DECLARE
    v_media NUMERIC;
BEGIN
    SELECT ROUND(AVG((nota1 + nota2) / 2)::numeric, 2)
      INTO v_media
      FROM matricula
     WHERE aluno_id = p_aluno_id
       AND nota1 IS NOT NULL AND nota2 IS NOT NULL;

    RETURN COALESCE(v_media, 0);
END;
$$
;;