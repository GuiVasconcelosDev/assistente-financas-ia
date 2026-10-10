package com.gui.financas.web;

import com.gui.financas.application.GastoService;
import com.gui.financas.domain.CategoriaGasto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Endpoints REST diretos (sem IA). Usam o mesmo GastoService, então as mesmas validações. */
@RestController
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse criar(@Valid @RequestBody GastoRequest request) {
        return GastoResponse.from(
                service.registrar(request.descricao(), request.valor(), request.categoria(), request.data()));
    }

    @GetMapping
    public List<GastoResponse> ultimos(@RequestParam(defaultValue = "10") int quantidade) {
        return service.ultimos(quantidade).stream().map(GastoResponse::from).toList();
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> total(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String categoria) {
        return Map.of("total", service.total(inicio, fim, categoria));
    }

    @GetMapping("/resumo")
    public Map<CategoriaGasto, BigDecimal> resumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.resumoPorCategoria(inicio, fim);
    }
}
