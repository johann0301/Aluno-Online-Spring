package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.RelatorioRequestDTO;
import br.com.alunoonline.api.service.RelatorioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@SecurityRequirement(name = "bearer-key")
public class RelatorioController {

    private final RelatorioService service;

    @Autowired
    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    // Lista views disponiveis (com labels) + colunas de cada uma (alimenta o front)
    @GetMapping("/metadata")
    public Map<String, RelatorioService.ViewMeta> metadata() {
        return service.metadata();
    }

    // Consulta interativa - retorna JSON
    @PostMapping
    public RelatorioService.RelatorioResultado consultar(
            @RequestBody RelatorioRequestDTO req) {
        return service.consultar(req);
    }

    // Mesma consulta, mas streaming CSV
    @PostMapping(value = "/export.csv", produces = "text/csv")
    public void exportarCsv(
            @RequestBody RelatorioRequestDTO req,
            HttpServletResponse response) throws IOException {

        RelatorioService.RelatorioResultado res = service.consultar(req);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"relatorio_" + req.getView() + ".csv\"");

        try (PrintWriter w = response.getWriter()) {
            // Header CSV usa labels amigaveis
            w.println(String.join(",", res.labels().stream()
                .map(RelatorioController::csvCell).toList()));

            for (Map<String, Object> linha : res.linhas()) {
                List<String> cells = res.campos().stream()
                    .map(c -> csvCell(linha.get(c)))
                    .toList();
                w.println(String.join(",", cells));
            }
        }
    }

    private static String csvCell(Object v) {
        if (v == null) return "";
        String s = v.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
