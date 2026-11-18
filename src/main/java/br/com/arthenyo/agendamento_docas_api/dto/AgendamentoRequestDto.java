package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.validation.Placa;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record AgendamentoRequestDto(
        @NotBlank(message = "O fornecedor é obrigatorio")
        Long codFornecedor,

        @NotBlank(message = "O Doca é obrigatorio")
        String codDoca,

        @NotBlank(message = "A placa do veículo é obrigatória")
        @Placa
        String placaVeiculo,

        LocalDateTime inicio,
        LocalDateTime fim,

        int quantidadePaletes,

        @NotBlank(message = "tipo carga é obrigatorio")
        TipoCarga tipoCarga,

        @NotBlank(message = "status é obrigatorio")
        StatusAgendamento status,

        String motivoCancelamento
) {
}
