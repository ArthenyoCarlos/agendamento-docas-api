package br.com.arthenyo.agendamento_docas_api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.arthenyo.agendamento_docas_api.dto.DocaRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.DocaResponseDto;
import br.com.arthenyo.agendamento_docas_api.service.DocaService;

@RestController 
@RequestMapping("/docas")
public class DocaController {

    private final DocaService docaService;

    public DocaController(DocaService docaService) {
        this.docaService = docaService;
    }

    @GetMapping
    public ResponseEntity<List<DocaResponseDto>> listarDocasAtivas(){
        return ResponseEntity.ok(docaService.listarDocasAtivas());
    }

    @PostMapping
    public ResponseEntity<DocaResponseDto> criarDoca(@RequestBody DocaRequestDto dto){
        DocaResponseDto salvarDoca = docaService.criarDoca(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
        .buildAndExpand(salvarDoca.id()).toUri();
        return ResponseEntity.created(uri).body(salvarDoca);
    }
    
}
