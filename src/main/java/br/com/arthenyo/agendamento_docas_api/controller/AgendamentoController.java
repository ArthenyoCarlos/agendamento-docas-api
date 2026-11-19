package br.com.arthenyo.agendamento_docas_api.controller;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.dto.CancelamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDto>> listarAgendaDocaPorData(
            @RequestParam Long docaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.listarAgendaDocaPorData(docaId, data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDto> buscarAgendamentoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentoPorId(id));
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDto> criarAgendamento(@RequestBody @Valid AgendamentoRequestDto dto) {
        AgendamentoResponseDto salvarAgendamento = agendamentoService.criarAgendamento(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(salvarAgendamento.id()).toUri();
        return ResponseEntity.created(uri).body(salvarAgendamento);
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<Void> realizarCheckIn(@PathVariable Long id) {
        agendamentoService.realizarCheckIn(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<Void> finalizar(@PathVariable Long id) {
        agendamentoService.finalizar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @RequestBody CancelamentoRequestDto dto) {
        agendamentoService.cancelar(id, dto.motivo());
        return ResponseEntity.noContent().build();
    }
}
