package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;

public record DocaResponseDto(
    Long id,
    String codigo,
    TipoDoca tipo,
    boolean ativo
) {

    public DocaResponseDto(Doca entity){
        this(entity.getId(),
            entity.getCodigo(),
            entity.getTipo(),
            entity.isAtiva());
    }
    
}
