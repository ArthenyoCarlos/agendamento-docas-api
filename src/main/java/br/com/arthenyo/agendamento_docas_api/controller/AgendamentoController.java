package br.com.arthenyo.agendamento_docas_api.controller;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDto> criarAgendamento(@RequestBody @Valid AgendamentoRequestDto dto){
        AgendamentoResponseDto salvarAgendamento = agendamentoService.criarAgendamento(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(salvarAgendamento.id()).toUri();
        return ResponseEntity.created(uri).body(salvarAgendamento);
    }
}
