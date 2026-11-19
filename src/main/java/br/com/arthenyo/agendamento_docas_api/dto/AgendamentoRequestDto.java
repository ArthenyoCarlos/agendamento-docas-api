package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.validation.Placa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record AgendamentoRequestDto(
        @NotNull(message = "O fornecedor é obrigatorio")
        Long fornecedorId,

        @NotNull(message = "O Doca é obrigatorio")
        Long docaId,

        @NotBlank(message = "A placa do veículo é obrigatória")
        @Placa(message = "Placa informada e invalida")
        String placaVeiculo,

        @NotNull(message = "O início é obrigatório")
        LocalDateTime inicio,

        @Min(1)
        @Max(26)
        int quantidadePaletes,

        @NotNull(message = "tipo carga é obrigatorio")
        TipoCarga tipoCarga
) {
}
