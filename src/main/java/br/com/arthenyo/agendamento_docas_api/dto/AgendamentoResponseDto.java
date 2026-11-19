package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;

import java.time.LocalDateTime;

public record AgendamentoResponseDto(
        Long id,
        String fornecedor,
        String doca,
        String placaVeiculo,
        LocalDateTime inicio,
        LocalDateTime fim,
        int quantidadePaletes,
        TipoCarga tipoCarga,
        StatusAgendamento status,
        String motivoCancelamento
) {
    public AgendamentoResponseDto(Agendamento entity){
        this(
                entity.getId(),
                entity.getFornecedor().getRazaoSocial(),
                entity.getDoca().getCodigo(),
                entity.getPlacaVeiculo(),
                entity.getInicio(),
                entity.getFim(),
                entity.getQuantidadePaletes(),
                entity.getTipoCarga(),
                entity.getStatus(),
                entity.getMotivoCancelamento()
        );
    }
}
