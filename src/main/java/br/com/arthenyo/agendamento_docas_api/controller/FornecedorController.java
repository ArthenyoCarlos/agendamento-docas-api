package br.com.arthenyo.agendamento_docas_api.controller;

import br.com.arthenyo.agendamento_docas_api.dto.FornecedorRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.FornecedorResponseDto;
import br.com.arthenyo.agendamento_docas_api.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponseDto> criarFornecedor(@RequestBody @Valid FornecedorRequestDto dto){
        FornecedorResponseDto salvarFornecedor = fornecedorService.criarFornecedor(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(salvarFornecedor.id()).toUri();
        return ResponseEntity.created(uri).body(salvarFornecedor);
    }
}
