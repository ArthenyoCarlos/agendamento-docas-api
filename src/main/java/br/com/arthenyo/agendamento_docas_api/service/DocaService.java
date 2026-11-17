package br.com.arthenyo.agendamento_docas_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.arthenyo.agendamento_docas_api.dto.DocaRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.DocaResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.repository.DocaRepository;

@Service 
public class DocaService {

    private final DocaRepository docaRepository;

    public DocaService(DocaRepository docaRepository) {
        this.docaRepository = docaRepository;
    }

    @Transactional(readOnly = true)
    public List<DocaResponseDto> listarDocasAtivas(){
        return docaRepository.findByAtivaTrue()
                    .stream()
                    .map(DocaResponseDto::new)
                    .toList();
    }

    @Transactional
    public DocaResponseDto criarDoca(DocaRequestDto request){
        Doca doca = new Doca();
        doca.setTipo(request.tipo());
        doca = docaRepository.save(doca);
        return new DocaResponseDto(doca);
    }
    
}
