package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@SecurityRequirement(name = "bearer-key")
public class DashboardController {

    private final DashboardService service;

    @Autowired
    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public Map<String, Object> kpis() {
        return service.kpis();
    }

    @GetMapping("/boletim/{alunoId}")
    public List<Map<String, Object>> boletim(@PathVariable Long alunoId) {
        return service.boletimAluno(alunoId);
    }

    @GetMapping("/matriculas-ativas")
    public List<Map<String, Object>> matriculasAtivas() {
        return service.matriculasAtivas();
    }

    @GetMapping("/aluno/{alunoId}/media")
    public Map<String, Object> mediaAluno(@PathVariable Long alunoId) {
        return service.mediaAluno(alunoId);
    }
}
