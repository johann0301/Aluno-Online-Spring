package br.com.alunoonline.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioRequestDTO {

    private String view;
    private List<String> campos;
    private List<Filtro> filtros;
    private String orderBy;
    private String orderDir;
    private Integer limit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filtro {
        private String campo;
        private String operador;
        private Object valor;
    }
}
