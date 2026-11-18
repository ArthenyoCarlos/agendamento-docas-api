package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.validation.Placa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequestDto(
        @NotNull(message = "O fornecedor é obrigatorio")
        Long fornecedorId,

        @NotNull(message = "O Doca é obrigatorio")
        Long docaId,

        @NotBlank(message = "A placa do veículo é obrigatória")
        @Placa(message = "Placa informada e invalida")
        String placaVeiculo,

        LocalDateTime inicio,
        LocalDateTime fim,

        int quantidadePaletes,

        @NotNull(message = "tipo carga é obrigatorio")
        TipoCarga tipoCarga,

        StatusAgendamento status,

        String motivoCancelamento
) {
}
