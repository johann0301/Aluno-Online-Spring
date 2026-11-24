package br.com.alunoonline.api.config;

import br.com.alunoonline.api.model.*;
import br.com.alunoonline.api.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired AlunoRepository alunoRepo;
    @Autowired ProfessorRepository profRepo;
    @Autowired DisciplinaRepository discRepo;
    @Autowired MatriculaRepository matRepo;
    @Autowired JdbcTemplate jdbcTemplate;

    @Value("${app.seed.reset:false}")
    private boolean resetSeed;

    @Override
    @Transactional
    public void run(String... args) {
        long matriculas = matRepo.count();
        if (!resetSeed && matriculas > 0) {
            log.info("[Seed] Banco ja populado (matriculas={}) - pulado.", matriculas);
            return;
        }

        if (resetSeed) {
            log.warn("[Seed] Reset ativo: limpando tabelas e recriando dados de demonstracao.");
        } else {
            log.info("[Seed] Banco sem matriculas: recriando dados de demonstracao.");
        }

        limparTabelas();

        log.info("[Seed] Populando banco com dados de demonstracao...");

        // ============ PROFESSORES ============
        Professor pJoao   = prof("Joao Silva",    "joao.silva@uniesp.edu.br",    "11122233344");
        Professor pMaria  = prof("Maria Santos",  "maria.santos@uniesp.edu.br",  "22233344455");
        Professor pCarlos = prof("Carlos Lima",   "carlos.lima@uniesp.edu.br",   "33344455566");
        Professor pAna    = prof("Ana Oliveira",  "ana.oliveira@uniesp.edu.br",  "44455566677");

        // ============ DISCIPLINAS ============
        Disciplina dAlgo  = disc("Algoritmos e Estrutura de Dados", 80, pJoao);
        Disciplina dBd    = disc("Banco de Dados II",               80, pMaria);
        Disciplina dWeb   = disc("Programacao Web",                 60, pCarlos);
        Disciplina dRedes = disc("Redes de Computadores",           60, pAna);
        Disciplina dEs    = disc("Engenharia de Software",          80, pCarlos);
        Disciplina dMat   = disc("Matematica Discreta",             60, pJoao);

        // ============ ALUNOS ============
        Aluno aPedro    = aluno("Pedro Henrique",   "pedro.h@aluno.uniesp.br",    "10000000001");
        Aluno aJulia    = aluno("Julia Costa",      "julia.c@aluno.uniesp.br",    "10000000002");
        Aluno aRafael   = aluno("Rafael Almeida",   "rafael.a@aluno.uniesp.br",   "10000000003");
        Aluno aBeatriz  = aluno("Beatriz Souza",    "beatriz.s@aluno.uniesp.br",  "10000000004");
        Aluno aLucas    = aluno("Lucas Pereira",    "lucas.p@aluno.uniesp.br",    "10000000005");
        Aluno aCarol    = aluno("Carolina Mendes",  "carolina.m@aluno.uniesp.br", "10000000006");
        Aluno aThiago   = aluno("Thiago Rocha",     "thiago.r@aluno.uniesp.br",   "10000000007");
        Aluno aAmanda   = aluno("Amanda Ribeiro",   "amanda.r@aluno.uniesp.br",   "10000000008");
        Aluno aGustavo  = aluno("Gustavo Carvalho", "gustavo.c@aluno.uniesp.br",  "10000000009");
        Aluno aFernanda = aluno("Fernanda Dias",    "fernanda.d@aluno.uniesp.br", "10000000010");

        // ============ MATRICULAS ============
        LocalDateTime semestreAnterior = LocalDateTime.now().minusDays(120);
        LocalDateTime semestreAtual    = LocalDateTime.now().minusDays(30);
        LocalDateTime trancouLaAtras   = LocalDateTime.now().minusDays(200);

        // Pedro
        matricula(aPedro, dAlgo, semestreAnterior, "8.5", StatusMatricula.APROVADO);
        matricula(aPedro, dBd,   semestreAtual,    null,  StatusMatricula.MATRICULADO);
        matricula(aPedro, dEs,   semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Julia (top)
        matricula(aJulia, dAlgo, semestreAnterior, "9.5", StatusMatricula.APROVADO);
        matricula(aJulia, dMat,  semestreAnterior, "9.8", StatusMatricula.APROVADO);
        matricula(aJulia, dBd,   semestreAtual,    "7.5", StatusMatricula.MATRICULADO);
        matricula(aJulia, dWeb,  semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Rafael
        matricula(aRafael, dAlgo,  semestreAnterior, "6.0", StatusMatricula.REPROVADO);
        matricula(aRafael, dBd,    semestreAtual,    null,  StatusMatricula.MATRICULADO);
        matricula(aRafael, dRedes, semestreAnterior, "5.5", StatusMatricula.TRANCADO);

        // Beatriz
        matricula(aBeatriz, dBd,  semestreAtual,    "9.0", StatusMatricula.MATRICULADO);
        matricula(aBeatriz, dMat, semestreAnterior, "8.7", StatusMatricula.APROVADO);
        matricula(aBeatriz, dEs,  semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Lucas (com reprovacao)
        matricula(aLucas, dAlgo,  semestreAnterior, "4.5", StatusMatricula.REPROVADO);
        matricula(aLucas, dMat,   semestreAnterior, "5.8", StatusMatricula.REPROVADO);
        matricula(aLucas, dRedes, semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Carolina (carga cheia)
        matricula(aCarol, dAlgo, semestreAnterior, "7.8", StatusMatricula.APROVADO);
        matricula(aCarol, dBd,   semestreAtual,    null,  StatusMatricula.MATRICULADO);
        matricula(aCarol, dWeb,  semestreAtual,    null,  StatusMatricula.MATRICULADO);
        matricula(aCarol, dEs,   semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Thiago
        matricula(aThiago, dAlgo, trancouLaAtras, null, StatusMatricula.TRANCADO);
        matricula(aThiago, dMat,  semestreAtual,  null, StatusMatricula.MATRICULADO);

        // Amanda
        matricula(aAmanda, dWeb,   semestreAtual,    "8.2", StatusMatricula.MATRICULADO);
        matricula(aAmanda, dRedes, semestreAtual,    null,  StatusMatricula.MATRICULADO);
        matricula(aAmanda, dEs,    semestreAnterior, "7.0", StatusMatricula.APROVADO);

        // Gustavo
        matricula(aGustavo, dAlgo, semestreAnterior, "7.2", StatusMatricula.APROVADO);
        matricula(aGustavo, dWeb,  semestreAtual,    null,  StatusMatricula.MATRICULADO);

        // Fernanda
        matricula(aFernanda, dMat, semestreAnterior, "6.5", StatusMatricula.REPROVADO);

        log.info("[Seed] Concluido: {} alunos, {} professores, {} disciplinas, {} matriculas.",
            alunoRepo.count(), profRepo.count(), discRepo.count(), matRepo.count());
    }

    private void limparTabelas() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE matricula, disciplina, aluno, professor RESTART IDENTITY CASCADE"
        );
    }

    private Professor prof(String name, String email, String cpf) {
        Professor p = new Professor();
        p.setName(name); p.setEmail(email); p.setCpf(cpf);
        return profRepo.save(p);
    }

    private Aluno aluno(String name, String email, String cpf) {
        Aluno a = new Aluno();
        a.setName(name); a.setEmail(email); a.setCpf(cpf);
        return alunoRepo.save(a);
    }

    private Disciplina disc(String nome, int ch, Professor prof) {
        Disciplina d = new Disciplina();
        d.setNome(nome); d.setCargaHoraria(ch); d.setProfessor(prof);
        return discRepo.save(d);
    }

    private void matricula(Aluno a, Disciplina d, LocalDateTime data,
                           String nota, StatusMatricula status) {
        Matricula m = new Matricula();
        m.setAluno(a); m.setDisciplina(d);
        m.setDataPrimeira(data);
        if (nota != null) {
            Double val = Double.valueOf(nota);
            m.setNota1(val);
            m.setNota2(val);
        }
        m.setStatus(status);
        matRepo.save(m);
    }
}
