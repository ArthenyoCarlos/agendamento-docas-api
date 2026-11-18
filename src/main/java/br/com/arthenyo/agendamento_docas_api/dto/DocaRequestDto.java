package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;
import jakarta.validation.constraints.NotBlank;

public record DocaRequestDto(
        @NotBlank(message = "O campo Tipo é Obrigatotio")
        TipoDoca tipo
) {
}
